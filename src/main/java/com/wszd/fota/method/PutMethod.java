package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author p14
 */
public class PutMethod implements WebDavMethod{
  @Override
  public String getMethodName() {
    return "PUT";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    if(webDavRequest.getWebDavPath().endsWith("/")){
      // PUT 不支持创建目录
      badRequest(webDavRequest,webDavResponse).onComplete(responseEndHandler);
      return;
    }
    webDavRequest.getRequest().body().compose(buffer -> getFileSystem(webDavEngine,webDavRequest).updateOrCreateFile(webDavRequest.getVertx(), webDavRequest.getWebDavPath(), buffer)
      .compose(event -> {
        withHtmlResponse(webDavResponse);
        return writeToResponse(String.format("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" +
          "<html><head>\n" +
          "<title>201 Created</title>\n" +
          "</head><body>\n" +
          "<h1>Created</h1>\n" +
          "<p>Resource %s has been created.</p>\n" +
          "</body></html>\n",webDavRequest.getWebDavPath()),webDavResponse.withStatus(HttpResponseStatus.CREATED.code()));
      })).onComplete(responseEndHandler);
  }
}
