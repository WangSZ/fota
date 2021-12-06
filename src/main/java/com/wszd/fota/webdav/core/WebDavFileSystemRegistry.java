package com.wszd.fota.webdav.core;

import com.wszd.fota.webdav.filesystem.LocalFileSystem;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 *
 * @author p14
 */
@Slf4j
public class WebDavFileSystemRegistry {
  private Vertx vertx;
  public static final ArrayList<IWebDavFileSystem> fileSystemCache=new ArrayList<>();

  public WebDavFileSystemRegistry(Vertx vertx) {
    this.vertx = vertx;
    init();
  }

  public void init(){
    String fileSystemPrefix="default.fileSystem.";
    String confPrefix="default.fileSystem.config.";
    JsonObject fileSystemConfig=new JsonObject();
    vertx.getOrCreateContext().config().forEach(stringObjectEntry -> {
      if(stringObjectEntry.getKey().startsWith(fileSystemPrefix)){
        fileSystemConfig.put(stringObjectEntry.getKey().substring(fileSystemPrefix.length()),stringObjectEntry.getValue());
      }
    });
    JsonObject config=new JsonObject();
    vertx.getOrCreateContext().config().forEach(stringObjectEntry -> {
      if(stringObjectEntry.getKey().startsWith(confPrefix)){
        config.put(stringObjectEntry.getKey().substring(confPrefix.length()),stringObjectEntry.getValue());
      }
    });
    IWebDavFileSystem webDavFileSystem;
    try {
      webDavFileSystem = (IWebDavFileSystem) WebDavFileSystemRegistry.class.getClassLoader().loadClass(fileSystemConfig.getString("class")).newInstance();
      webDavFileSystem.init(vertx,config,this).onFailure(event -> {
        log.error(event.getLocalizedMessage(),event);
      });
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    log.info("using fileSystem {}",webDavFileSystem);
    fileSystemCache.add(webDavFileSystem);
  }

  /**
   * 判断 webdav uri应该使用哪个，文件系统
   * @param webDavPath
   * @return
   */
  public IWebDavFileSystem select(WebDavRequest webDavPath){
    // TODO 根据用户筛选
    return fileSystemCache.stream().findFirst().get();
  }

}
