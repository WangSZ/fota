package com.wszd.fota.method;

import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.webdav.core.WebDavResponseHeader;
import com.wszd.fota.xml.dav.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import static com.wszd.fota.xml.dav.LockToken.OPAQUELOCK_TOKEN;

/**
 * 不实现 lock，永远返回 lock 成功
 * @author p14
 */
@Slf4j
public class LockMethod extends AbstractWebDavMethodWithResponse{
  @Override
  public String getMethodName() {
    return "LOCK";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    withXmlResponse(webDavResponse);
    Future<Void> fu = webDavRequest.getRequest().body().map(buffer -> {
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
      webDavResponse.setHeader(WebDavResponseHeader.LOCK_TOKEN,String.format("<%s>",lockToken));
      log.debug("response body {}",responseBody);
      writeOKToResponseWithLength(responseBody,webDavRequest,webDavResponse,responseEndHandler);
      return null;
    });
    fu.onComplete(responseEndHandler);
  }
}
