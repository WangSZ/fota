package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.webdav.core.WebDavResponseHeader;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * @author p14
 */
@Slf4j
public class MoveMethod implements WebDavMethod {
  @Override
  public String getMethodName() {
    return "MOVE";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    boolean overwrite=isOverwrite(webDavRequest.getRequest());
    String targetLocation= URI.create(getDestination(webDavRequest.getRequest())).getPath().substring(webDavResponse.getWebDavRoot().length());
    log.debug("move from {} to {}",webDavRequest.getWebDavPath(),targetLocation);

    Future<Void> moveFuture ;
    if (webDavRequest.getWebDavPath().endsWith("/")) {
      moveFuture=getFileSystem(webDavEngine, webDavRequest).moveDirectory(webDavRequest.getVertx(), webDavRequest.getWebDavPath(), targetLocation,overwrite);
    } else {
      moveFuture=getFileSystem(webDavEngine, webDavRequest).moveFile(webDavRequest.getVertx(), webDavRequest.getWebDavPath(), targetLocation,overwrite);
    }
    moveFuture.compose(event -> {
      webDavResponse.withHeader(WebDavResponseHeader.LOCATION,targetLocation);
      webDavResponse.withStatus(HttpResponseStatus.CREATED.code());
      return writeToResponse(null,webDavResponse);
    }).onComplete(responseEndHandler);
  }
}
