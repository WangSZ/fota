package com.wszd.fota.webdav.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.RoutingContext;

/**
 * @author p14
 */
public interface FotaAuthenticationProvider extends AuthenticationProvider {
  boolean isEnable();
  boolean accept(RoutingContext event);
  JsonObject getCredentials(RoutingContext event);

  default String getChallenge(RoutingContext event){
    String authorization = event.request().getHeader("authorization");
    if(authorization!=null){
      return authorization.substring(0,authorization.indexOf(" "));
    }else {
      return "";
    }
  }

}
