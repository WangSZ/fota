package com.wszd.fota.webdav.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.wszd.fota.webdav.core.WebDavRequestHeader.CONTENT_LENGTH;

/**
 * @author p14
 */
@Data
@Slf4j
public class WebDavResponse {
  int status;
  String statusMessage;
  HttpServerResponse response;
  Context context;
  Vertx vertx;
  String webDavRoot;

  public WebDavResponse withContentLength(int length){
    withHeader(CONTENT_LENGTH, String.valueOf(length));
    return this;
  }

  public WebDavResponse withStatus(int status){
    setStatus(status);
    ensureMessage();
    return this;
  }

  public WebDavResponse ensureStatus(){
    if(getStatus()<=0){
      log.warn("response status not set!");
      setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }
    return this;
  }

  public WebDavResponse ensureMessage(){
    ensureStatus();
    if(getStatusMessage()==null){
      setStatusMessage(HttpResponseStatus.valueOf(getStatus()).reasonPhrase());
    }
    return this;
  }

  public WebDavResponse withHeader(String name, String value){
    getResponse().putHeader(name,value);
    return this;
  }
}
