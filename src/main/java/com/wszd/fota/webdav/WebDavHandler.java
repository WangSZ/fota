package com.wszd.fota.webdav;

import com.wszd.fota.util.URLUtil;
import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.webdav.core.WebDavResponseHeader;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;
import io.vertx.core.*;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author p14
 */
@Slf4j
public class WebDavHandler implements Handler<RoutingContext> {
  private Vertx vertx;
  private WebDavEngine webDavEngine;
  private String rootPath;

  public WebDavHandler(String rootPath, Vertx vertx) {
    this.vertx = vertx;
    this.rootPath=rootPath;
    webDavEngine = new WebDavEngine(vertx);
  }

  @Override
  public void handle(RoutingContext ctx) {
    log.debug("request {} {} {}", ctx.request().method(), ctx.request().uri(), ctx.request().version());
    WebDavRequest webDavRequest = new WebDavRequest();
    webDavRequest.setWebDavPath(URLUtil.decodeUrl(ctx.request().uri()).substring(rootPath.length()));
    webDavRequest.setMethod(ctx.request().method().name());
    webDavRequest.setRequest(ctx.request());
    webDavRequest.setContext(ctx.vertx().getOrCreateContext());
    webDavRequest.setVertx(ctx.vertx());
    if(log.isDebugEnabled()){
      ctx.request().headers().forEach(entry -> {
        log.debug("request {}: {}", entry.getKey(), entry.getValue());
      });
    }

    WebDavResponse webDavResponse = new WebDavResponse();
    // 默认请求失败
    webDavResponse.setStatus(0);
    webDavResponse.setWebDavRoot(rootPath);
    webDavResponse.setResponse(ctx.response());
    webDavResponse.setVertx(ctx.vertx());
    webDavResponse.setContext(ctx.vertx().getOrCreateContext());
    webDavEngine.handle(webDavRequest, webDavResponse, event -> {
      if (event.succeeded()) {
        if(webDavResponse.getStatus()<=0){
          webDavResponse.withStatus(HttpResponseStatus.OK.code());
        }
        if (log.isDebugEnabled()) {
          webDavResponse.getResponse().headers().forEach(entry -> {
            log.debug("response {}: {}", entry.getKey(), entry.getValue());
          });
          log.debug("response {}", webDavResponse);
        }
      } else {
        if(webDavResponse.getStatus()<=0){
          webDavResponse.withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
        if (event.cause() instanceof IOException) {
          log.debug("unhandled IOException ", event.cause());
        } else  if (event.cause() instanceof FileNotFoundException) {
          webDavResponse.withStatus(HttpResponseStatus.NOT_FOUND.code());
        } else {
          log.error("unhandled exception {}",event.cause(), event.cause());
        }
      }

      if (!webDavResponse.getResponse().headWritten()) {
        webDavResponse.getResponse().setChunked(true);
        webDavResponse.getResponse().setStatusCode(webDavResponse.ensureStatus().getStatus());
        webDavResponse.getResponse().setStatusMessage(webDavResponse.ensureMessage().getStatusMessage());
        webDavResponse.getResponse().end();
      }
    });
  }
}
