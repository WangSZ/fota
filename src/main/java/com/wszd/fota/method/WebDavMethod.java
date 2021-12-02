package com.wszd.fota.method;

import com.wszd.fota.webdav.core.IWebDavFileSystem;
import com.wszd.fota.webdav.core.WebDavEngine;
import com.wszd.fota.webdav.core.WebDavRequest;
import com.wszd.fota.webdav.core.WebDavResponse;
import com.wszd.fota.webdav.core.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

import java.io.FileNotFoundException;

/**
 * @author p14
 */
public interface WebDavMethod {
  String getMethodName();

  void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler);

  default void withXmlResponse(WebDavResponse webDavResponse){
    webDavResponse.setHeader(WebDavResponseHeader.CONTENT_TYPE, WebDavResponseHeader.CONTENT_TYPE_XML_UTF8);
  }

  default void withHtmlResponse(WebDavResponse webDavResponse){
    webDavResponse.setHeader(WebDavResponseHeader.CONTENT_TYPE, WebDavResponseHeader.CONTENT_TYPE_HTML_UTF8);
  }

  default void fileNotFound(WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    webDavResponse.withContentLength(page404.length());
    webDavResponse.withStatus(HttpResponseStatus.NOT_FOUND.code());
    webDavResponse.getResponse().end(page404);
    responseEndHandler.handle(Future.succeededFuture());
  }

  default IWebDavFileSystem getFileSystem(WebDavEngine webDavEngine, WebDavRequest webDavRequest){
    return webDavEngine.getFileSystem(webDavRequest);
  }

  /**
   * Depth = "Depth" ":" ("0" | "1" | "infinity")
   * @param request
   * @return
   */
  default int getDepth(HttpServerRequest request) {
    if(!request.headers().contains(WebDavRequestHeader.DEPTH)){
      return 0;
    }
    if(request.getHeader(WebDavRequestHeader.DEPTH).equalsIgnoreCase("infinity")){
      return Integer.MAX_VALUE;
    }
    int depth = Integer.parseInt(request.getHeader(WebDavRequestHeader.DEPTH));
    if(depth!=0&&depth!=1){
      throw new IllegalArgumentException("depth must be in (0,1,infinity)");
    }
    return depth;
  }

  /**
   *  Overwrite = "Overwrite" ":" ("T" | "F")
   * @param request
   * @return
   */
  default boolean isOverwrite(HttpServerRequest request){
    if(request.headers().contains(WebDavRequestHeader.OVERWRITE)&& WebDavRequestHeader.OVERWRITE_T.equals(request.getHeader(WebDavRequestHeader.OVERWRITE))){
      return true;
    }
    return false;
  }

  /**
   *  Destination: http://localhost:9999/7739AAC0.tmp
   * @param request
   * @return
   */
  default String getDestination(HttpServerRequest request){
    return request.getHeader(WebDavRequestHeader.DESTINATION);
  }


  default void responseSuccess(WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler){
    if(webDavResponse.getStatus()==0){
      webDavResponse.setStatus(HttpResponseStatus.OK.code());
    }
    responseEndHandler.handle(Future.succeededFuture());
  }

  default void responseError(WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler,Throwable t){
    if(t instanceof FileNotFoundException){
      webDavResponse.setStatus(HttpResponseStatus.NOT_FOUND.code());
    }else {

      if(webDavResponse.getStatus()==0){
        webDavResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      }
    }
    responseEndHandler.handle(Future.failedFuture(t));
  }

  default void responseError(WebDavResponse webDavResponse,int httpCode, Handler<AsyncResult<Void>> responseEndHandler){
    if(webDavResponse.getStatus()==0){
      webDavResponse.setStatus(httpCode);
    }
    responseEndHandler.handle(Future.succeededFuture());
  }

  default void responseBadRequest(WebDavResponse webDavResponse,Handler<AsyncResult<Void>> responseEndHandler){
    if(webDavResponse.getStatus()==0){
      webDavResponse.setStatus(HttpResponseStatus.BAD_REQUEST.code());
    }
    responseEndHandler.handle(Future.succeededFuture());
  }

  /**
   * DAV = "DAV" ":" "1" ["," "2"] ["," 1#extend]
   * Destination = "Destination" ":" absoluteURI
   * Lock-Token ="Lock-Token"":"Coded-URL
   * If ="If"":" (1*No-tag-list | 1*Tagged-list)
   * Lock-Token ="Lock-Token"":"Coded-URL
   * Overwrite ="Overwrite"":" ("T" |"F")·
   *
   * TimeOut = "Timeout" ":" 1#TimeType
   * TimeType = ("Second-" DAVTimeOutVal | "Infinite" | Other)
   * DAVTimeOutVal = 1*digit
   * Other = "Extend" field-value
   *
   * opaquelocktoken
   */
  String XML_STATUS_OK="HTTP/1.1 200 OK";
  String page404="<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" +
    "<html><head>\n" +
    "<title>404 Not Found</title>\n" +
    "</head><body>\n" +
    "<h1>Not Found</h1>\n" +
    "<p>The requested URL was not found on this server.</p>\n" +
    "</body></html>\n";
}
