package com.wszd.fota.method;

import com.wszd.fota.util.DateUtil;
import com.wszd.fota.webdav.core.*;
import com.wszd.fota.xml.dav.*;
import com.wszd.fota.xml.win.Win32Prop;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * @author p14
 */
@Slf4j
public class PropFindMethod extends AbstractWebDavMethodWithResponse {
  @Override
  public String getMethodName() {
    return "PROPFIND";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    withXmlResponse(webDavResponse);
    webDavRequest.getRequest().body().map(buffer -> {
      log.debug("PropFind {}",buffer.toString());
      IWebDavFileSystem fileSystem=getFileSystem(webDavEngine,webDavRequest);
      fileSystem.getFileObject(webDavRequest.getWebDavPath()).onSuccess(fileObject->{
        if(!fileObject.isExist()){
          fileNotFound(webDavRequest,webDavResponse,responseEndHandler);
          return;
        }else {
          int depth=getDepth(webDavRequest.getRequest());
          fileSystem.listFile(webDavRequest.getWebDavPath(),depth).onSuccess(fileList->{
            if(fileList.size()==0){
              fileNotFound(webDavRequest, webDavResponse, responseEndHandler);
              return;
            }else {;
              webDavResponse.setStatus(HttpResponseStatus.MULTI_STATUS.code());
              MultiStatus multiStatus=new MultiStatus();
              multiStatus.setResponseList(new ArrayList<>());
              fileList.forEach(f-> multiStatus.getResponseList().add(fileObjectToResponse(f)));
              writeToResponseWithLength(XmlUtil.objectToXml(multiStatus,Win32Prop.class),webDavRequest,webDavResponse,responseEndHandler);
              return;
            }
          }).onFailure(t-> responseEndHandler.handle(Future.failedFuture(t)));
        }
      }).onFailure(t-> responseEndHandler.handle(Future.failedFuture(t)));

      return null;
    });
  }

  private Response fileObjectToResponse(FileObject f){
    Response r=new Response();
    r.setHref(Response.encodeHref(f.getWebDavPath()));
    PropStat propStat=new PropStat();
    propStat.setStatus(XML_STATUS_OK);
    Win32Prop prop=new Win32Prop();
    // TODO etag 由文件系统生成
    prop.setETag(String.format("W/%s %s %s",f.getCreationDate(),f.getLastModified(),f.getContentLength()).hashCode()+"");
    prop.setContentLength(f.getContentLength());
    prop.setLastModified(DateUtil.getStringISO8601(f.getLastModified()));
    prop.setCreationDate(DateUtil.getStringISO8601(f.getCreationDate()));
    prop.setContentType(f.getContentType());

    prop.setWin32CreationTime(DateUtil.getStringISO8601(f.getWin32CreationTime()));
    prop.setWin32LastAccessTime(DateUtil.getStringISO8601(f.getWin32LastAccessTime()));
    prop.setWin32LastModifiedTime(DateUtil.getStringISO8601(f.getWin32LastModifiedTime()));
    prop.setWin32FileAttributes(f.getWin32FileAttributes());

    if(f.isDirectory()){
      prop.setResourceType(ResourceType.COLLECTION);
    }
    // TODO ? 默认支持吗
    prop.setSupportedlock(Supportedlock.DEFAULT);
    propStat.setProp(prop);
    r.setPropStat(propStat);
    log.debug("{}",r);
    return r;
  }

}
