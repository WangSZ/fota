package com.wszd.fota.webdav;

import com.wszd.fota.webdav.auth.DigestAuth;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

/**
 * @author p14
 */
class DigestAuthTest {

  @Test
  public void testAuthenticate() throws ExecutionException, InterruptedException {
/**
 * -pair  username="fota"
 * -pair  realm="fota"
 * -pair  nonce="c59bf1ef-b8c9-4323-8ff2-c891a9805036"
 * -pair  uri="/"
 * -pair  algorithm=MD5-sess
 * -pair  response="ce74a5d69968a2f534553e880f0fa1cb"
 * -pair  qop=auth
 * -pair  nc=00000002
 * -pair  cnonce="c8f2a079f99aff74"
 * s {"username":"fota","algorithm":"MD5-sess","cnonce":"c8f2a079f99aff74","method":"GET","nc":"00000002","nonce":"c59bf1ef-b8c9-4323-8ff2-c891a9805036","qop":"auth","realm":"fota","response":"ce74a5d69968a2f534553e880f0fa1cb","uri":"/"}
 * se ce74a5d69968a2f534553e880f0fa1cb  expect f85029ef58b7070161204c5e2ffc4c3d
 * -user auth fail {"username":"fota","realm":"fota","nonce":"c59bf1ef-b8c9-4323-8ff2-c891a9805036","uri":"/","algorithm":"MD5-sess","response":"ce74a5d69968a2f534553e880f0fa1cb","qop":"auth","nc":"00000002","cnonce":"c8f2a079f99aff74","method":"GET"} Bad response
 * Impl -0:0:0:0:0:0:0:1 - GET / HTTP/1.1 401 0 - 1 ms
 */

    String passwd=String.format("%s:fota:%s","fota","fota");
    DigestAuth digestAuth=new DigestAuth(passwd);
    JsonObject credentials=new JsonObject();
    credentials.put("username","fota");
    credentials.put("realm","fota");
    credentials.put("nonce","c59bf1ef-b8c9-4323-8ff2-c891a9805036");
    credentials.put("uri","/");
    credentials.put("algorithm","MD5-sess");
    credentials.put("response","ce74a5d69968a2f534553e880f0fa1cb");
    credentials.put("qop","auth");
    credentials.put("nc","00000002");
    credentials.put("cnonce","c8f2a079f99aff74");
    credentials.put("method","GET");
    System.out.println(digestAuth.authenticate(credentials).toCompletionStage().toCompletableFuture().get().principal());
    Assertions.assertEquals(digestAuth.authenticate(credentials).toCompletionStage().toCompletableFuture().get().principal().getString("username"),"fota");
  }

}
