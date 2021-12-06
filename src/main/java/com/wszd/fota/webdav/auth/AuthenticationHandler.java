package com.wszd.fota.webdav.auth;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  @author p14
 */
@Slf4j
public class AuthenticationHandler implements Handler<RoutingContext> {
  private final FotaAuthenticationProvider[] authenticationProviderList;
  private Vertx vertx;

  public AuthenticationHandler(Vertx vertx, FotaAuthenticationProvider...authenticationProvider) {
    this.vertx=vertx;
    authenticationProviderList=authenticationProvider;
  }

  @Override
  public void handle(RoutingContext event) {
    for (FotaAuthenticationProvider authenticationProvider : authenticationProviderList) {
      if (authenticationProvider.isEnable()&&authenticationProvider.accept(event)) {
        JsonObject credentials = authenticationProvider.getCredentials(event);
        String blacklistKey=getBlackListKey(event,credentials);
        // 防止爆破密码
        if(blacklistKey!=null){
          Integer errorCount = blackList.get(blacklistKey);
          if(errorCount!=null&&errorCount>denyCount){
            log.warn("blacklist hit {}",blacklistKey);
            // 如果处于黑名单中，则假装验证失败，返回模拟的认证失败信息以欺骗攻击者继续爆破
            onAuthFail(event);
            return;
          }
        }
        authenticationProvider.authenticate(credentials, ar -> {
          if (ar.succeeded()) {
            log.debug("user auth success {} request {}", ar.result().principal(), event.request().uri());
            event.next();
          } else {
            log.debug("user auth fail {} {}", credentials, ar.cause().getLocalizedMessage());
            if (blacklistKey != null) {
              addBlacklistWhenAuthFail(event, blacklistKey);
            }
            onAuthFail(event);
          }
        });
        return;
      }
    }
    log.info("no auth provider");
    onAuthFail(event);
  }

  private void onAuthFail(RoutingContext event){
    event.response().putHeader("WWW-Authenticate",String.format("Digest realm=\"fota\", nonce=\"%s\", algorithm=MD5, qop=\"auth\"", UUID.randomUUID().toString().replaceAll("-","")));
    event.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
  }

  private String getBlackListKey(RoutingContext event,
                                 JsonObject credentials){
    String username=credentials.getString("username");
    if(username==null){
      return null;
    }
    String address = event.request().remoteAddress().host();
    return username+":"+address;
  }

  /**
   * 普通用户短时间内只会输入一次密码，只有爆破的请求才可能连续尝试密码。假设 普通人 输入密码间隔为 2 秒，那么输错密码 2 秒内
   * 为了误导爆破者，只要输错一次密码，就把用户+ip加入黑名单，后续尝试直接返回密码错误，让爆破这误以为没有被发现而继续攻击（实际上攻击请求中的正确密码也会被返回为错误，从而误导攻击者）。
   * 2 秒后从黑名单移除，此时正常用户尝试密码就能正常通过了
   * @param event
   * @param blacklistKey
   */
  private void addBlacklistWhenAuthFail(RoutingContext event, String blacklistKey){
    log.warn("add blacklist {} for {}",blacklistKey,blacklistTtl);
    blackList.compute(blacklistKey,(k,v)->{
      if(v==null){
        event.vertx().setTimer(blacklistTtl,i->{
          log.info("remove blacklist {}",blacklistKey);
          blackList.remove(blacklistKey);
        });
        return v;
      }
      return v+1;
    });
  }

  private int blacklistTtl=2000;
  private int denyCount=5;
  private final ConcurrentHashMap<String,Integer> blackList=new ConcurrentHashMap<>();
}
