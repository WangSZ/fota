package com.wszd.fota;

import com.wszd.fota.webdav.FileNotFoundException;
import com.wszd.fota.webdav.auth.AuthenticationHandler;
import com.wszd.fota.webdav.auth.BasicAuth;
import com.wszd.fota.webdav.auth.DigestAuth;
import com.wszd.fota.webdav.WebDavHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author p14
 */
@Slf4j
public class FotaVerticle extends AbstractVerticle {

  private String fotaPrefix="fota.";

  public void afterConfigParsed(JsonObject config) {
    System.getenv().forEach((k,v)->{
      log.info("getenv {} {}",k,v);
      if(k.startsWith(fotaPrefix)){
        config.put(k.substring(fotaPrefix.length()),v);
      }
    });
    System.getProperties().forEach((k,v)->{
      log.info("getProperties {} {}",k,v);
      if(k.toString().startsWith(fotaPrefix)){
        config.put(k.toString().substring(fotaPrefix.length()),v);
      }
    });
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port=Integer.parseInt(vertx.getOrCreateContext().config().getString("webdav.port","9999"));
    String rootPath=vertx.getOrCreateContext().config().getString("webdav.root","/webdav");
    log.info("config {}",vertx.getOrCreateContext().config());
    Router router = Router.router(vertx);
    router.route().failureHandler(event -> {
      if(event.failure() instanceof FileNotFoundException){
        event.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
      }else {
        event.next();
      }
    });
    router.route().handler(event -> {
      event.response().putHeader("Server","FOTA").putHeader("Date",new Date().toString());
      event.next();
    });
    router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));
    router.route(rootPath+"/*").handler(new AuthenticationHandler(vertx,new DigestAuth(vertx),new BasicAuth(vertx))).handler(new WebDavHandler(rootPath,vertx));
    router.route("/*").handler(ctx->{
      ctx.redirect(rootPath+ctx.request().uri());
    });
    vertx.createHttpServer().requestHandler(router).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        log.info("FOTA(File on the air) server started on port " + port +" with root path "+rootPath);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
