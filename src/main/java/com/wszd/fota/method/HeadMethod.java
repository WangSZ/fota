package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author p14
 */
public class HeadMethod implements WebDavMethod{
  @Override
  public String getMethodName() {
    return "HEAD";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    webDavResponse.withHeader("DAV","1");
    // TODO 缺少 allow header
    ok(webDavResponse).onComplete(responseEndHandler);
  }
}
