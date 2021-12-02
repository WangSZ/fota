package com.wszd.fota.webdav.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import lombok.Data;

import static com.wszd.fota.webdav.core.WebDavRequestHeader.CONTENT_LENGTH;

/**
 * @author p14
 */
@Data
public class WebDavResponse {
  int status;
  String statusMessage;
  HttpServerResponse response;
  Context context;
  Vertx vertx;

  public WebDavResponse withContentLength(int length){
    getResponse().setStatusCode(getStatus())
      .setStatusMessage(getStatusMessage()==null? HttpResponseStatus.valueOf(getStatus()).reasonPhrase(): getStatusMessage());
    setHeader(CONTENT_LENGTH, String.valueOf(length));
    return this;
  }
  public WebDavResponse withStatus(int status){
    setStatus(status);
    getResponse().setStatusCode(getStatus())
      .setStatusMessage(getStatusMessage()==null? HttpResponseStatus.valueOf(getStatus()).reasonPhrase(): getStatusMessage());
    return this;
  }
  public WebDavResponse setHeader(String name,String value){
    getResponse().putHeader(name,value);
    return this;
  }
}
