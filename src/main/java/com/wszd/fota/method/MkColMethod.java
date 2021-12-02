package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author p14
 */
@Slf4j
public class MkColMethod implements WebDavMethod {
  @Override
  public String getMethodName() {
    return "MKCOL";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    log.debug("MKCOL from {}",webDavRequest.getWebDavPath());
    getFileSystem(webDavEngine, webDavRequest).makeDirectory(webDavRequest.getVertx(),webDavRequest.getWebDavPath()).onComplete(responseEndHandler);

  }
}
