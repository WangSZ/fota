package com.wszd.fota;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author p14
 */
@Slf4j
public class FotaLauncher extends Launcher {
  private String fotaPrefix="fota.";

  public static void main(String[] args) {
    new FotaLauncher().dispatch(args);
  }

  @Override
  public void afterConfigParsed(JsonObject config) {
    super.afterConfigParsed(config);
    System.getenv().forEach((k,v)->{
      if(k.startsWith(fotaPrefix)){
        log.info("add config from env {} {}",k,v);
        config.put(k.substring(fotaPrefix.length()),v);
      }
    });
  }
}
