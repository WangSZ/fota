package com.wszd.fota.method;

import com.wszd.fota.webdav.core.IWebDavFileSystem;
import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.xml.win.Win32Prop;
import com.wszd.fota.xml.dav.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * @author p14
 */
@Slf4j
public class PropPatchMethod extends AbstractWebDavMethodWithResponse{
  @Override
  public String getMethodName() {
    return "PROPPATCH";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    webDavRequest.getRequest().body().map(buffer -> {
      String body = buffer.toString();
      log.debug(" patch {}",body);
      Object obj = XmlUtil.xmlToObject(body, Propertyupdate.class);
      if(obj instanceof Propertyupdate){
        Propertyupdate propertyupdate = (Propertyupdate) obj;
        IWebDavFileSystem fileSystem = getFileSystem(webDavEngine, webDavRequest);
        fileSystem.updateProperties(webDavRequest.getVertx(),webDavRequest.getWebDavPath(),propertyupdate)
          .onSuccess(event -> {
            withXmlResponse(webDavResponse);
            MultiStatus multiStatus=new MultiStatus();
            multiStatus.setResponseList(new ArrayList<>());
            Response r = new Response();
            multiStatus.getResponseList().add(r);
            r.setHref(Response.encodeHref(webDavRequest.getWebDavPath()));
            r.setPropStat(new PropStat(propertyupdate.getSet().getProp()));
            r.getPropStat().setStatus(XML_STATUS_OK);
            String bodyResponse=XmlUtil.objectToXml(multiStatus, Win32Prop.class);
            log.debug("response body {}",bodyResponse);
            webDavResponse.withStatus(HttpResponseStatus.MULTI_STATUS.code());
            writeToResponseWithLength(bodyResponse,webDavRequest,webDavResponse,responseEndHandler);
          })
          .onComplete(responseEndHandler);
      }else {
        responseBadRequest(webDavResponse,responseEndHandler);
        return null;
      }
      return null;
    });
  }
}
