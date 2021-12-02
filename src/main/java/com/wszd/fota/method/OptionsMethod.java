package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author p14
 */
public class OptionsMethod implements WebDavMethod{
  @Override
  public String getMethodName() {
    return "OPTIONS";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    webDavResponse.setHeader("DAV","1");
    // TODO 支持 2
    responseSuccess(webDavResponse,responseEndHandler);
  }
}
