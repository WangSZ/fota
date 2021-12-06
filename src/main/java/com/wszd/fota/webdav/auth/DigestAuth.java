package com.wszd.fota.webdav.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.htdigest.HtdigestCredentials;
import io.vertx.ext.auth.impl.UserImpl;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static io.vertx.ext.auth.impl.Codec.base16Encode;

/** copy from vertx-auth-htdigest
 *
 * @author p14
 */
@Slf4j
public class DigestAuth implements FotaAuthenticationProvider {

  public DigestAuth(String passwd) {
    init(passwd);
  }

  public DigestAuth(Vertx vertx) {
    if(vertx.getOrCreateContext().config().containsKey("username")){
      // load the file into memory
      // username:real:password
      // TODO 重构?
      String username=vertx.getOrCreateContext().config().getString("username","fota");
      String password=vertx.getOrCreateContext().config().getString("password","fota");
      String passwd=String.format("%s:fota:%s",username,password);
      log.warn("username is {} password is {}",username,password);
      init(passwd);
    }
  }

  private void init(String passwd){
    String realm = null;
    for (String line : passwd.split("\\r?\\n")) {
      String[] parts = line.split(":");
      if (realm == null) {
        realm = parts[1];
      } else {
        if (!realm.equals(parts[1])) {
          throw new RuntimeException("multiple realms in htdigest file not allowed.");
        }
      }
      htdigest.put(parts[0], new Digest(parts[0], parts[1], parts[2]));
    }
  }

  @Override
  public boolean isEnable() {
    return htdigest.size()>0;
  }

  @Override
  public boolean accept(RoutingContext event) {
    return "digest".equalsIgnoreCase(getChallenge(event));
  }

  @Override
  public JsonObject getCredentials(RoutingContext event) {
    JsonObject credentials=new JsonObject();
    String authorization = event.request().getHeader("authorization");
    if(authorization!=null){
      authorization=authorization.trim();
      Arrays.asList(authorization.substring(authorization.indexOf(" ")).split(",")).stream().map(pair->{
        int index = pair.indexOf("=");
        if(index>=0){
          return new String[]{pair.substring(0,index).trim(),pair.substring(index+1).trim()};
        }else {
          return new String[]{pair,""};
        }
      }).map(pair->{
//         去掉开头和结尾的双引号
        if(pair[1].startsWith("\"")){
          pair[1]=pair[1].substring(1,pair[1].length()-1);
        }
        return pair;
      }).forEach(pair->{
        credentials.put(pair[0],pair[1]);
      });
      credentials.put("method",event.request().method().name());
    }
    return credentials;
  }

  private static class Digest {
    final String username;
    final String realm;
    final String password;

    Digest(String username, String realm, String password) {

      this.username = username;
      this.realm = realm;
      this.password = password;
    }
  }

  private final ConcurrentHashMap<String, Digest> htdigest = new ConcurrentHashMap<>();

  @Override
  public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
    authenticate(new HtdigestCredentials(authInfo), resultHandler);
  }

  @Override
  public void authenticate(Credentials credentials, Handler<AsyncResult<User>> resultHandler) {
    try {
      HtdigestCredentials authInfo = (HtdigestCredentials) credentials;
      authInfo.checkValid(null);

      if (!htdigest.containsKey(authInfo.getUsername())) {
        resultHandler.handle((Future.failedFuture("Unknown username.")));
        return;
      }

      final Digest credential = htdigest.get(authInfo.getUsername());

      if (!credential.realm.equals(authInfo.getRealm())) {
        resultHandler.handle((Future.failedFuture("Invalid realm.")));
        return;
      }
      // MD5(MD5(A1):<nonce>:<nc>:<cnonce>:<qop>:MD5(A2))
      // h1 = md5(a1) h2=md5(a2)

      // calculate ha1
      final String ha1;
      if ("MD5-sess".equals(authInfo.getAlgorithm())) {
        ha1 =md5(md5(credential.username+":"+credential.realm+":"+ credential.password) + ":" + authInfo.getNonce() + ":" + authInfo.getCnonce());
      } else if ("MD5".equals(authInfo.getAlgorithm())) {
        ha1 =md5(credential.username+":"+credential.realm+":"+ credential.password);
      }else {
        resultHandler.handle((Future.failedFuture("algorithm: " +authInfo.getAlgorithm()+" not supported.")));
        return;
      }

      // calculate ha2
      final String ha2;
      if (null==authInfo.getQop()||"auth".equals(authInfo.getQop())) {
        ha2 = md5(authInfo.getMethod() + ":" + authInfo.getUri());
      } else if ("auth-int".equals(authInfo.getQop())) {
        resultHandler.handle((Future.failedFuture("qop: auth-int not supported.")));
        return;
      } else {
          resultHandler.handle((Future.failedFuture("Invalid qop "+authInfo.getQop())));
          return;
      }

      // calculate request digest
      final String digest;
      if (authInfo.getQop() == null) {
        // For RFC 2069 compatibility
        digest = md5(ha1 + ":" + authInfo.getNonce() + ":" + ha2);
      } else {
        digest = md5(ha1 + ":" + authInfo.getNonce() + ":" + authInfo.getNc() + ":" + authInfo.getCnonce() + ":" + authInfo.getQop() + ":" + ha2);
      }

      if (digest.equals(authInfo.getResponse())) {
        resultHandler.handle(Future.succeededFuture(new UserImpl(new JsonObject().put("username", credential.username).put("realm", credential.realm))));
      } else {
        log.info("bad response {}  expect {}",authInfo.getResponse(),digest);
        resultHandler.handle(Future.failedFuture("Bad response"));
      }
    } catch (RuntimeException e) {
      resultHandler.handle(Future.failedFuture(e));
    }
  }

  private String md5(String payload) {
    MessageDigest MD5;
    try {
      MD5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return base16Encode(MD5.digest(payload.getBytes(StandardCharsets.UTF_8)));
  }

}
