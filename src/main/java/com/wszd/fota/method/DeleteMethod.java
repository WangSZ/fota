package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author p14
 */
public class DeleteMethod implements WebDavMethod {
  @Override
  public String getMethodName() {
    return "DELETE";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    int depth = getDepth(webDavRequest.getRequest());

    if (webDavRequest.getWebDavPath().endsWith("/")) {
//      if (depth != Integer.MAX_VALUE) { // 有些client不传这个header
//        responseError(webDavResponse, HttpResponseStatus.BAD_REQUEST.code(), responseEndHandler);
//      } else {
        getFileSystem(webDavEngine, webDavRequest).deleteDirectory(webDavRequest.getVertx(),webDavRequest.getWebDavPath(), true).onComplete(responseEndHandler);
//      }
    } else {
      getFileSystem(webDavEngine, webDavRequest).deleteFile(webDavRequest.getVertx(),webDavRequest.getWebDavPath()).onComplete(responseEndHandler);
    }
  }
}
