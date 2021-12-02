package com.wszd.fota.webdav.filesystem;

import com.wszd.fota.webdav.core.FileObject;
import com.wszd.fota.webdav.core.IBlockingWebDavFileSystem;
import com.wszd.fota.webdav.core.IWebDavFileSystem;
import com.wszd.fota.webdav.core.WebDavFileSystemRegistry;
import com.wszd.fota.xml.dav.Propertyupdate;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author p14
 */
@Slf4j
public class BlockingFileSystemDelegate implements IWebDavFileSystem {
  private IBlockingWebDavFileSystem delegate;
  private WorkerExecutor worker;
  private JsonObject config;
  private String name;


  @Override
  public String name() {
    return name;
  }

  @Override
  public Future<Void> init(Vertx vertx, JsonObject config, WebDavFileSystemRegistry registry) {
    String delegateClass=config.getString("delegate");
    this.name=config.getString("name",this.toString());
    this.config=config;
    try {
      delegate= (IBlockingWebDavFileSystem) BlockingFileSystemDelegate.class.getClassLoader().loadClass(delegateClass).newInstance();
      delegate.init(vertx,config,registry);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    worker = vertx.createSharedWorkerExecutor(config.getString("worker.name","blocking-file"), config.getInteger("worker.size", 200 * Runtime.getRuntime().availableProcessors()));
    return Future.succeededFuture();
  }

  @Override
  public Future<FileObject> getFileObject(String webDavPath) {
    return worker.executeBlocking(promise->{
      try{
        promise.complete(delegate.getFileObject(webDavPath));
      }catch (Throwable t){
        promise.fail(t);
      }
    });
  }

  @Override
  public Future<List<FileObject>> listFile(String webDavPath, int depth) {
    return null;
  }

  @Override
  public Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject, long begin, long end) {
    return null;
  }

  @Override
  public Future<Void> updateOrCreateFile(Vertx vertx, String webDavPath, Buffer buffer) {
    return null;
  }

  @Override
  public Future<Void> deleteFile(Vertx vertx, String webDavPath) {
    return null;
  }

  @Override
  public Future<Void> deleteDirectory(Vertx vertx, String webDavPath, boolean recursive) {
    return null;
  }

  @Override
  public Future<Void> moveFile(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    return null;
  }

  @Override
  public Future<Void> moveDirectory(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    return null;
  }

  @Override
  public Future<Void> makeDirectory(Vertx vertx, String webDavPath) {
    return null;
  }

  @Override
  public Future<Void> updateProperties(Vertx vertx, String webDavPath, Propertyupdate propertyupdate) {
    return null;
  }
}
