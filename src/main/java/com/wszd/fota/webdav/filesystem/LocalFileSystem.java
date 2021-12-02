package com.wszd.fota.webdav.filesystem;

import com.wszd.fota.webdav.core.FileObject;
import com.wszd.fota.webdav.core.IWebDavFileSystem;
import com.wszd.fota.webdav.core.WebDavFileSystemRegistry;
import com.wszd.fota.xml.dav.Propertyupdate;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author p14
 */
@Slf4j
public class LocalFileSystem implements IWebDavFileSystem {
  private JsonObject config;
  private String name;
  @Override
  public String name() {
    return name;
  }

  @Override
  public Future<Void> init(Vertx vertx, JsonObject config, WebDavFileSystemRegistry registry){
    this.config=config;
    this.name=config.getString("name",this.toString());
    File f = new File(config.getString("root","."));
    if(!f.exists()){
      throw new IllegalStateException("root directory not exist"+root);
    }
    log.debug("root {} directory is {}",root,f.getAbsolutePath());
    this.root =f.getAbsolutePath() ;
    return Future.succeededFuture();
  }

  private String root;
  /**
   * TODO ignoreFile 改为可配置
   */
  private HashSet<String> ignoreFile=new HashSet<>(Arrays.asList("System Volume Information"));

  public LocalFileSystem() {
  }

  @Override
  public Future<FileObject> getFileObject(String webDavPath) {
    log.debug("getFileObject {}",webDavPath);
    File file=new File(getPathOnFileSystem(webDavPath));
    FileObject f = FileObject.newObjectFromFile(webDavPath, file);
    log.debug("FileObject {}",f);
    return Future.succeededFuture(f);
  }

  @Override
  public Future<List<FileObject>> listFile(String webDavPath, int depth) {
    log.debug("listFile webDavPath= {} ,depth={}",webDavPath,depth);
    String pathOnFileSystem=getPathOnFileSystem(webDavPath);
    log.debug("listFile webDavPath= {} ,pathOnFileSystem={} ,depth={}",webDavPath,pathOnFileSystem,depth);
    List<FileObject> arr=new ArrayList<>();
    listFiles(webDavPath,arr,new File(pathOnFileSystem),0,depth);
    return Future.succeededFuture(arr);
  }


  private void listFiles(String currentWebDavPath,List<FileObject> fileObjectList,File file,int currentDepth,int maxDepth){
    if(currentDepth>maxDepth){
      return;
    }
    if(!file.canRead()){
      log.debug("can not read file {} {}",currentWebDavPath,file);
      return;
    }
    if(file.isHidden()){
      log.debug("skip hidden file {} {}",currentWebDavPath,file);
      return;
    }
    if(ignoreFile.contains(file.getName())){
      log.debug("ignore read file {} {}",currentWebDavPath,file);
      return;
    }
    log.debug("listFiles {} {}",currentWebDavPath,file);

    if(file!=null&&file.exists()){
      if(file.isDirectory()){
        fileObjectList.add(FileObject.newObjectFromFile(currentWebDavPath,file));
        File[] files = file.listFiles();
        for (int i = 0; i <files.length; i++) {
          File child=files[i];
          listFiles(currentWebDavPath+(currentWebDavPath.endsWith("/")?"":"/")+child.getName(), fileObjectList, child, currentDepth+1, maxDepth);
        }
      }else {
        fileObjectList.add(FileObject.newObjectFromFile(currentWebDavPath,file));
      }
    }
  }


  @Override
  public Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject, long begin, long end) {
    log.debug("readFile {} {}-{}",fileObject.getWebDavPath(),begin,end);
    long length=end-begin+1;
    return vertx.fileSystem().open(getPathOnFileSystem(fileObject.getWebDavPath()),new OpenOptions().setRead(true)).map(asyncFile -> {
      asyncFile.setReadPos(begin);
      asyncFile.setReadLength(length);
      return asyncFile;
    });
  }

  @Override
  public Future<Void> updateOrCreateFile(Vertx vertx, String webDavPath, Buffer buffer) {
    log.debug("updateOrCreateFile {}",webDavPath);
    String filePath = getPathOnFileSystem(webDavPath);
    return vertx.fileSystem().writeFile(filePath,buffer);
  }

  private String getPathOnFileSystem(String webDavPath){
    if(webDavPath.contains("/..")){
      // TODO 怎么防御遍历目录？
      throw new IllegalArgumentException("path error "+webDavPath);
    }
    return root+webDavPath;
  }

  @Override
  public Future<Void> deleteFile(Vertx vertx, String webDavPath) {
    log.debug("deleteFile {} ",webDavPath);
    return vertx.fileSystem().delete(getPathOnFileSystem(webDavPath));
  }

  @Override
  public Future<Void> deleteDirectory(Vertx vertx, String webDavPath, boolean recursive) {
    log.debug("deleteDirectory {}  recursive {}",webDavPath,recursive);
    return vertx.fileSystem().deleteRecursive(getPathOnFileSystem(webDavPath),recursive);
  }

  @Override
  public Future<Void> moveFile(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    log.debug("moveFile {} to {} overwrite {}",webDavPath,targetLocation,overwrite);
    return vertx.fileSystem().move(getPathOnFileSystem(webDavPath),getPathOnFileSystem(targetLocation),new CopyOptions().setReplaceExisting(overwrite));
  }

  @Override
  public Future<Void> moveDirectory(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    log.debug("moveDirectory {} to {} overwrite {}",webDavPath,targetLocation,overwrite);
    return vertx.fileSystem().move(getPathOnFileSystem(webDavPath),getPathOnFileSystem(targetLocation),new CopyOptions().setReplaceExisting(overwrite));
  }

  @Override
  public Future<Void> makeDirectory(Vertx vertx, String webDavPath) {
    log.debug("makeDirectory {} ",webDavPath);
    return vertx.fileSystem().mkdir(getPathOnFileSystem(webDavPath));
  }

  @Override
  public Future<Void> updateProperties(Vertx vertx, String webDavPath, Propertyupdate propertyupdate) {
    log.debug("updateProperties {} {}",webDavPath,propertyupdate);
    // TODO 忽略属性
    return Future.succeededFuture();
  }
}
