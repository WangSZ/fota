package com.wszd.fota.webdav.core;

import com.wszd.fota.method.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author p14
 */
public class WebDavEngine {
  private static final ConcurrentHashMap<String, WebDavMethod> ALL_METHODS =new ConcurrentHashMap<String,WebDavMethod>();

  private final WebDavFileSystemRegistry webDavFileSystemRegistry;

  public WebDavEngine(Vertx vertx) {
    webDavFileSystemRegistry=new WebDavFileSystemRegistry(vertx);
    init();
  }

  private void init() {
    reg(new PropFindMethod());
    reg(new GetMethod());
    reg(new OptionsMethod());
    reg(new HeadMethod());
    reg(new PutMethod());
    reg(new PropPatchMethod());
    reg(new DeleteMethod());
    reg(new MoveMethod());
    reg(new MkColMethod());
    reg(new LockMethod());
    reg(new UnlLockMethod());
  }

  public void handle(WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler){
    WebDavMethod method = ALL_METHODS.get(webDavRequest.getMethod().toUpperCase());
    if(method==null){
      webDavResponse.getResponse().setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code());
      responseEndHandler.handle(Future.failedFuture((new IllegalArgumentException("method not supported :"+webDavRequest.getMethod()))));
    }else {
      method.handle(this,webDavRequest, webDavResponse, responseEndHandler);
    }
  }

  private void reg(WebDavMethod method){
    ALL_METHODS.put(method.getMethodName(),method);
  }

  public IWebDavFileSystem getFileSystem(WebDavRequest webDavRequest){
    return webDavFileSystemRegistry.select(webDavRequest);
  }

}
