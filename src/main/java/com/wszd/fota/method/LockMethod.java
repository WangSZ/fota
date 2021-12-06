package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.webdav.core.WebDavResponseHeader;
import com.wszd.fota.xml.dav.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import static com.wszd.fota.xml.dav.LockToken.OPAQUELOCK_TOKEN;

/**
 * 不实现 lock，永远返回 lock 成功
 * @author p14
 */
@Slf4j
public class LockMethod  implements WebDavMethod{
  @Override
  public String getMethodName() {
    return "LOCK";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    withXmlResponse(webDavResponse);
    webDavRequest.getRequest().body().compose(buffer -> {
      String body = buffer.toString();
      log.debug("lock {}",body);
      Object obj = XmlUtil.xmlToObject(body, LockInfo.class);
      String responseBody="";
      String lockToken=String.format("%s:none",OPAQUELOCK_TOKEN);
      if(obj instanceof LockInfo){
        LockInfo lockInfo= (LockInfo) obj;
        Prop prop=new Prop();
        prop.setLockDiscovery(new LockDiscovery(new ActiveLock(lockInfo.getLockType(),lockInfo.getLockScope(),"Infinity", lockInfo.getOwner(),"Second-604800" ,new LockToken(lockToken))));
        responseBody=XmlUtil.objectToXml(prop);
      }
      webDavResponse.withHeader(WebDavResponseHeader.LOCK_TOKEN,String.format("<%s>",lockToken));
      return writeToResponse(responseBody,webDavResponse.withStatus(HttpResponseStatus.MULTI_STATUS.code()));
    }).onComplete(responseEndHandler);
  }
}
