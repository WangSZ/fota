package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author p14
 */
@Slf4j
public abstract class AbstractWebDavMethodWithResponse implements WebDavMethod{

  public void writeToResponseWithLength(String body, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    log.debug("response body({}) {}",body.length(),body);
    webDavResponse.withContentLength(body.length());
    webDavResponse.getResponse().write(body);
    webDavResponse.getResponse().end();
    responseEndHandler.handle(Future.succeededFuture());
  }
  public void writeOKToResponseWithLength(String body, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    log.debug("response body({}) {}",body.length(),body);
    webDavResponse.withContentLength(body.length());
    webDavResponse.withStatus(HttpResponseStatus.OK.code());
    webDavResponse.getResponse().write(body);
    webDavResponse.getResponse().end();
    responseEndHandler.handle(Future.succeededFuture());
  }

}
