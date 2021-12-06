package com.wszd.fota.webdav.filesystem;

import com.wszd.fota.webdav.FileNotFoundException;
import com.wszd.fota.webdav.core.FileObject;
import com.wszd.fota.webdav.core.IWebDavFileSystem;
import com.wszd.fota.webdav.core.WebDavFileSystemRegistry;
import com.wszd.fota.xml.dav.Propertyupdate;
import io.netty.util.internal.StringUtil;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 聚合多个 IWebDavFileSystem 为一个。
 * 假设映射关系如下 ：
 * /    -> /mnt@Local
 * /abc -> /@RemoteSamba
 * /abc/def -> /data/mirror@Local
 * 那么：
 * 当用户访问 /media/a.mp3 时，会使用 /mnt@Local
 * 当用户访问 /abc/b.mp3 时，会使用 /@RemoteSamba
 * 当用户访问 /abc/def/c.mp3 时，会使用 /data/mirror@Local
 *
 * @author p14
 */
@Slf4j
public class UnionWebDavFileSystem implements IWebDavFileSystem {
  static class Node {
    private String path;
    private IWebDavFileSystem iWebDavFileSystem;

    public Node(String path, IWebDavFileSystem iWebDavFileSystem) {
      this.path = path;
      this.iWebDavFileSystem = iWebDavFileSystem;
    }
  }

  private ConcurrentHashMap<String, Node> webDavFileSystems = new ConcurrentHashMap<>();
  private List<String> fileSystemSortedByPrefix;

  private JsonObject config;
  private String name;
  private Vertx vertx;
  private WorkerExecutor worker;

  @Override
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "UnionWebDavFileSystem{" +
      "name='" + name() + "[" + webDavFileSystems.entrySet().stream().map(en -> String.format("%s->%s", en.getKey(), en.getValue().toString())).collect(Collectors.joining(";")) +
      "]" + '\'' +
      '}';
  }

  @Override
  public Future<Void> init(Vertx vertx, JsonObject config, WebDavFileSystemRegistry registry) {
    this.config = config;
    this.vertx = vertx;
    this.name = config.getString("name", this.toString());
    worker = vertx.createSharedWorkerExecutor(config.getString("worker.name", "union-bio"), Integer.parseInt(config.getString("worker.size", "" + 20 * Runtime.getRuntime().availableProcessors())), 2000, TimeUnit.MILLISECONDS);
    List<String> includeFileSystemNames = Arrays.asList(config.getString("include", "").split(","));
    log.info("found fileSystems {}", includeFileSystemNames);
    for (String prefix : includeFileSystemNames) {
      JsonObject innerConfig = new JsonObject();
      log.debug("config {}", config);
      config.forEach(en -> {
        if (en.getKey().startsWith(prefix + ".config.")) {
          innerConfig.put(en.getKey().substring((prefix + ".config.").length()), en.getValue());
        }
      });
      log.debug("innerConfig {}", innerConfig);
      JsonObject innerSysConfig = new JsonObject();
      config.forEach(en -> {
        if (en.getKey().startsWith(prefix + ".")) {
          innerSysConfig.put(en.getKey().substring((prefix + ".").length()), en.getValue());
        }
      });
      log.debug("innerSysConfig {}", innerSysConfig);
      String clazz = innerSysConfig.getString("class");
      String path = innerSysConfig.getString("path");
      String error="";
      if(path.contains("//")){
        error="path should start with / ";
      }
      if(path.substring(1).contains("/")){
        error="path should start with / and the depth must not bigger than 1";
      }
      if(!error.isEmpty()){
        return Future.failedFuture(error);
      }
      IWebDavFileSystem webDavFileSystem;
      try {
        Object instance = UnionWebDavFileSystem.class.getClassLoader().loadClass(clazz).newInstance();
        if (instance instanceof IWebDavFileSystem) {
          webDavFileSystem = (IWebDavFileSystem) instance;
        } else if (instance instanceof IBlockingWebDavFileSystem) {
          webDavFileSystem = new BlockingFileSystemDelegate();
          innerConfig.put("delegate", clazz);
        } else {
          return Future.failedFuture("FileSystem must bu instance of IWebDavFileSystem or BlockingFileSystemDelegate");
        }
      } catch (Throwable e) {
        return Future.failedFuture(e);
      }
      webDavFileSystem.init(vertx, innerConfig, registry);
      webDavFileSystems.put(path, new Node(path, webDavFileSystem));
    }
    fileSystemSortedByPrefix = webDavFileSystems.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    log.info("ordered file {}", fileSystemSortedByPrefix);
    return Future.succeededFuture();
  }

  /**
   * 判断 请求路径 是否能在允许范围之内
   * @param webDavPath
   * @return 返回任何值，都表示检测不通过。如果检测通过，会返回null
   */
  private boolean checkValidPath(String webDavPath){
    if("/".equals(webDavPath)|| StringUtil.isNullOrEmpty(webDavPath)){
      return true;
    }
    if(!webDavPath.startsWith("//")&&webDavPath.startsWith("/")){
      String path = webDavPath.substring(1);
      int index = path.indexOf("/");
      if(index>0){
        path= path.substring(0,index);
      }
      if(webDavFileSystems.containsKey("/"+path)){
        return true;
      }
    }
    return false;
  }


  private Future<Node> findFirst(String webDavPath) {
    for (int i = 0; i < fileSystemSortedByPrefix.size(); i++) {
      String path = fileSystemSortedByPrefix.get(i);
      Node fs = webDavFileSystems.get(path);
      if (webDavPath.startsWith(path)) {
        return Future.succeededFuture(fs);
      }
    }
    return Future.failedFuture("cant not find file system");
  }

  private Node findFirstBlock(String webDavPath) {
    for (int i = 0; i < fileSystemSortedByPrefix.size(); i++) {
      String path = fileSystemSortedByPrefix.get(i);
      Node fs = webDavFileSystems.get(path);
      if (webDavPath.startsWith(path)) {
        return fs;
      }
    }
    throw new RuntimeException("cant not find file system");
  }

  /**
   * uri         -> /webdav/abc/def/123.txt
   * webDavPath  -> /abc/def/123.txt
   * 子系统 path  -> /abc
   * 实际文件位置  -> d:/tmp/def/123.txt
   *
   * @param webDavPath
   * @return
   */
  @Override
  public Future<FileObject> getFileObject(String webDavPath) {
    log.debug("getFileObject [{}]",webDavPath);
    if(!checkValidPath(webDavPath)){
      return Future.succeededFuture(FileObject.fakeFile(webDavPath,webDavPath,false,false));
    }
    for (int i = 0; i < fileSystemSortedByPrefix.size(); i++) {
      String fileSystemPath = fileSystemSortedByPrefix.get(i);
      Node fs = webDavFileSystems.get(fileSystemPath);
      if (webDavPath.startsWith(fileSystemPath)) {
        return fs.iWebDavFileSystem.getFileObject(getPathForChildFileSystem(fileSystemPath, webDavPath)).map(fileObject -> {
          fileObject.setWebDavPath(rewriteWebDavPathToParent(fileSystemPath,fileObject.getWebDavPath()));
          return fileObject;
        });
      }
    }
    if(webDavPath.equals("/")||webDavPath.isEmpty()){
      return Future.succeededFuture(FileObject.fakeDirectory(webDavPath,webDavPath));
    }
    return Future.failedFuture("cant not find file system for "+webDavPath);
  }

  private String getPathForChildFileSystem(String fileSystemPath, String webDavPath) {
    return webDavPath.substring(fileSystemPath.length());
  }

  private String rewriteWebDavPathToParent(String fileSystemPath, String childWebDavPath) {
    return fileSystemPath+childWebDavPath;
  }


  @Override
  public Future<List<FileObject>> listFile(String webDavPath, int depth) {
    checkValidPath(webDavPath);
    log.debug("listFile {} depth {}",webDavPath,depth);
    log.debug("getFileObject [{}]",webDavPath);
    if(!checkValidPath(webDavPath)){
      return Future.succeededFuture(Arrays.asList(FileObject.fakeFile(webDavPath,webDavPath,false,false)));
    }
    Promise<List<FileObject>> promise = Promise.promise();
    if("/".equals(webDavPath)||webDavPath.isEmpty()){
      List<FileObject> fileObjectList = new ArrayList<>();
      if(depth>=0){
        fileObjectList.add(FileObject.fakeDirectory(webDavPath,webDavPath));
      }
      if(depth>=1){
        webDavFileSystems.values().forEach(node -> {
          String path=rewriteWebDavPathToParent(node.path,webDavPath);
          fileObjectList.add(FileObject.fakeDirectory(path,path));
        });
      }
      return Future.succeededFuture(fileObjectList);
    }else {
      return findFirst(webDavPath).flatMap(node -> {
        return node.iWebDavFileSystem.listFile(getPathForChildFileSystem(node.path,webDavPath),depth).map(fileObjects -> {
          fileObjects.forEach(fileObject -> {
            fileObject.setWebDavPath(rewriteWebDavPathToParent(node.path,fileObject.getWebDavPath()));
          });
          return fileObjects;
        });
      });
    }
  }


  @Override
  public Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject, long begin, long end) {
    checkValidPath(fileObject.getWebDavPath());
    return findFirst(fileObject.getWebDavPath()).flatMap(fs -> {
      FileObject childFileObject = fileObject.clone();
      childFileObject.setWebDavPath(getPathForChildFileSystem(fs.path,fileObject.getWebDavPath()));
      return fs.iWebDavFileSystem.readFile(vertx, childFileObject, begin, end);
    });
  }

  @Override
  public Future<Void> updateOrCreateFile(Vertx vertx, String webDavPath, Buffer buffer) {
    checkValidPath(webDavPath);
    return findFirst(webDavPath).flatMap(fs -> fs.iWebDavFileSystem.updateOrCreateFile(vertx, getPathForChildFileSystem(fs.path,webDavPath), buffer));
  }

  @Override
  public Future<Void> deleteFile(Vertx vertx, String webDavPath) {
    checkValidPath(webDavPath);
    return findFirst(webDavPath).flatMap(fs -> fs.iWebDavFileSystem.deleteFile(vertx, getPathForChildFileSystem(fs.path,webDavPath)));
  }

  @Override
  public Future<Void> deleteDirectory(Vertx vertx, String webDavPath, boolean recursive) {
    checkValidPath(webDavPath);
    return findFirst(webDavPath).flatMap(fs -> fs.iWebDavFileSystem.deleteDirectory(vertx, getPathForChildFileSystem(fs.path,webDavPath),recursive));
  }

  @Override
  public Future<Void> moveFile(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    checkValidPath(webDavPath);
    checkValidPath(targetLocation);
    if(findFirstBlock(webDavPath).path.equals(findFirstBlock(targetLocation).path)){
      return findFirst(webDavPath).flatMap(node -> node.iWebDavFileSystem.moveFile(vertx,getPathForChildFileSystem(node.path,webDavPath),getPathForChildFileSystem(node.path,targetLocation),overwrite));
    }else {
      // TODO 跨文件系统的复制 待实现
      return Future.failedFuture("move to another FileSystem is not supported");
    }
  }

  @Override
  public Future<Void> moveDirectory(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    checkValidPath(webDavPath);
    checkValidPath(targetLocation);
    if(findFirstBlock(webDavPath).path.equals(findFirstBlock(targetLocation).path)){
      return findFirst(webDavPath).flatMap(node -> node.iWebDavFileSystem.moveDirectory(vertx,getPathForChildFileSystem(node.path,webDavPath),getPathForChildFileSystem(node.path,targetLocation),overwrite));
    }else {
      // TODO 跨文件系统的复制 待实现
      return Future.failedFuture("move to another FileSystem is not supported");
    }
  }

  @Override
  public Future<Void> makeDirectory(Vertx vertx, String webDavPath) {
    checkValidPath(webDavPath);
    return findFirst(webDavPath).flatMap(fs -> fs.iWebDavFileSystem.makeDirectory(vertx, getPathForChildFileSystem(fs.path,webDavPath)));
  }

  @Override
  public Future<Void> updateProperties(Vertx vertx, String webDavPath, Propertyupdate propertyupdate) {
    checkValidPath(webDavPath);
    return findFirst(webDavPath).flatMap(fs -> fs.iWebDavFileSystem.updateProperties(vertx, getPathForChildFileSystem(fs.path,webDavPath),propertyupdate));
  }
}
