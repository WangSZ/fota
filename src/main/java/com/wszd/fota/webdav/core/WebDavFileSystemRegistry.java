package com.wszd.fota.webdav.core;

import com.wszd.fota.webdav.filesystem.LocalFileSystem;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * // TODO 增加无法挂载到本地的文件系统
 * @author p14
 */
public class WebDavFileSystemRegistry {
  private Vertx vertx;
  public static final ArrayList<IWebDavFileSystem> fileSystemCache=new ArrayList<>();

  public WebDavFileSystemRegistry(Vertx vertx) {
    this.vertx = vertx;
    init();
  }

  public void init(){
    // TODO 根据配置文件初始化
    LocalFileSystem f = new LocalFileSystem();
    f.init(vertx,new JsonObject(),this);
    fileSystemCache.add(f);
  }

  public void reg(IWebDavFileSystem webDavFileSystem){

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
