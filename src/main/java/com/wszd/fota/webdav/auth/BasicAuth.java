package com.wszd.fota.webdav.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.impl.UserImpl;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author p14
 */
@Slf4j
public class BasicAuth  implements FotaAuthenticationProvider {

  public BasicAuth(Vertx vertx) {
    if(vertx.getOrCreateContext().config().containsKey("username")){
      String username=vertx.getOrCreateContext().config().getString("username","fota");
      String password=vertx.getOrCreateContext().config().getString("password","fota");
      String passwd=String.format("%s:%s",username,password);
      log.warn("username is {} password is {}",username,password);
      init(passwd);
    }
  }

  public BasicAuth(String passwd) {
    init(passwd);
  }


  private final ConcurrentHashMap<String, String> authCache = new ConcurrentHashMap<>();

  private void init(String passwd){
    for (String line : passwd.split("\\r?\\n")) {
      int index=line.indexOf(":");
      authCache.put(line.substring(0,index), line.substring(index+1));
    }
  }

  @Override
  public boolean isEnable() {
    return authCache.size()>0;
  }

  @Override
  public boolean accept(RoutingContext event) {
    return "basic".equalsIgnoreCase(getChallenge(event));
  }

  @Override
  public JsonObject getCredentials(RoutingContext event) {
    JsonObject jsonObject=new JsonObject();
    String authorization = event.request().getHeader("authorization");
    if(authorization!=null){
      authorization=authorization.trim();
      String base64 = authorization.substring(authorization.indexOf(" "));
      String up=new String(Base64.decodeBase64(base64.getBytes(StandardCharsets.UTF_8)));
      String[] arr = up.split(":");
      if(arr.length==2){
        jsonObject.put("username",arr[0]);
        jsonObject.put("password",arr[1]);
      }
    }
    return jsonObject;
  }

  @Override
  public void authenticate(JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
    String username = jsonObject.getString("username");
    String password = jsonObject.getString("password");
    String userPassword = authCache.get(username);
    if(userPassword!=null&&userPassword.equals(password)){
      JsonObject user = new JsonObject();
      handler.handle(Future.succeededFuture(new UserImpl(user)));
    }else {
      handler.handle(Future.failedFuture("invalid username or password"));
    }
  }
}
