package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * 不实现 unlock 永远返回成功
 * @author p14
 */
public class UnlLockMethod implements WebDavMethod{
  @Override
  public String getMethodName() {
    return "UNLOCK";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    // TODO
    ok(webDavResponse).onComplete(responseEndHandler);
  }
}
