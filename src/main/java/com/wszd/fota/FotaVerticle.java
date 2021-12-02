package com.wszd.fota;

import com.wszd.fota.webdav.WebDavHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author p14
 */
@Slf4j
public class FotaVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port=vertx.getOrCreateContext().config().getInteger("http.port",9999);
    String rootPath=vertx.getOrCreateContext().config().getString("webdav.root.path","");
    log.info("config {}",vertx.getOrCreateContext().config());

    Router router = Router.router(vertx);

    router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));
    router.route(rootPath+"/*").handler(new WebDavHandler(vertx));
    router.route("/*").handler(ctx->{
      ctx.redirect(rootPath);
    });

    vertx.createHttpServer().requestHandler(router).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        log.info("WEBDAV server started on port " + port +" with root path "+rootPath);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
