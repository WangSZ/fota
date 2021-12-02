package com.wszd.fota.webdav.core;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import lombok.Data;


/**
 * @author p14
 */
@Data
public class WebDavRequest {
  private String webDavPath;
  private String method;
  private HttpServerRequest request;
  private Context context;
  private Vertx vertx;
}
