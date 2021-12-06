package com.wszd.fota.webdav.filesystem;

import com.wszd.fota.webdav.core.FileObject;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 把同步io的文件系统转化为异步接口
 * @author p14
 */
@Slf4j
public class BlockingFileSystemDelegate implements IWebDavFileSystem {
  private IBlockingWebDavFileSystem delegate;
  private WorkerExecutor worker;
  private JsonObject config;
  private String name;

  @Override
  public String toString() {
    return "BlockingFileSystemDelegate{" +
      "name='" + name() + '\'' +
      '}';
  }

  @Override
  public String name() {
    return name+"@"+delegate.name();
  }

  private Future<Void> runBlockingVoid(Runnable runnable){
    return worker.executeBlocking(promise->{
      try{
        runnable.run();
        promise.complete();
      }catch (Throwable t){
        promise.fail(t);
      }
    });
  }

  private <T> Future<T> runBlocking(Callable<T> runnable){
    return worker.executeBlocking(promise->{
      try{
        promise.complete(runnable.call());
      }catch (Throwable t){
        promise.fail(t);
      }
    });
  }


  @Override
  public Future<Void> init(Vertx vertx, JsonObject config, WebDavFileSystemRegistry registry) {
    worker = vertx.createSharedWorkerExecutor(config.getString("worker.name","delegate-bio"),Integer.parseInt(config.getString("worker.size", ""+20 * Runtime.getRuntime().availableProcessors())),2000, TimeUnit.MILLISECONDS);
    String delegateClass=config.getString("delegate");
    this.name=config.getString("name",this.toString());
    this.config=config;
    try {
      delegate= (IBlockingWebDavFileSystem) BlockingFileSystemDelegate.class.getClassLoader().loadClass(delegateClass).newInstance();
      return runBlockingVoid(()-> delegate.init(vertx,config,registry));
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<FileObject> getFileObject(String webDavPath) {
    return runBlocking(()-> delegate.getFileObject(webDavPath));
  }

  @Override
  public Future<List<FileObject>> listFile(String webDavPath, int depth) {
    return runBlocking(()-> delegate.listFile(webDavPath,depth));
  }

  @Override
  public Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject, long begin, long end) {
    return runBlocking(()-> delegate.readFile(vertx,fileObject,begin,end));
  }

  @Override
  public Future<Void> updateOrCreateFile(Vertx vertx, String webDavPath, Buffer buffer) {
    return runBlockingVoid(()-> delegate.updateOrCreateFile(vertx,webDavPath,buffer));
  }

  @Override
  public Future<Void> deleteFile(Vertx vertx, String webDavPath) {
    return runBlockingVoid(()-> delegate.deleteFile(vertx,webDavPath));
  }

  @Override
  public Future<Void> deleteDirectory(Vertx vertx, String webDavPath, boolean recursive) {
    return runBlockingVoid(()-> delegate.deleteDirectory(vertx,webDavPath,recursive));
  }

  @Override
  public Future<Void> moveFile(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    return runBlockingVoid(()-> delegate.moveFile(vertx,webDavPath,targetLocation,overwrite));
  }

  @Override
  public Future<Void> moveDirectory(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite) {
    return runBlockingVoid(()-> delegate.moveDirectory(vertx,webDavPath,targetLocation,overwrite));
  }

  @Override
  public Future<Void> makeDirectory(Vertx vertx, String webDavPath) {
    return runBlockingVoid(()-> delegate.makeDirectory(vertx,webDavPath));
  }

  @Override
  public Future<Void> updateProperties(Vertx vertx, String webDavPath, Propertyupdate propertyupdate) {
    return runBlockingVoid(()-> delegate.updateProperties(vertx,webDavPath,propertyupdate));
  }
}
