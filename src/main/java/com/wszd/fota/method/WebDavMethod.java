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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * @author p14
 */
public interface WebDavMethod {
  Logger log=LoggerFactory.getLogger(WebDavMethod.class);
  String getMethodName();

  void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler);

  default Future<Void> writeToResponse(String body,WebDavResponse webDavResponse) {
    return writeToResponse(body,webDavResponse,true);
  }

  default Future<Void> writeOKToResponse(String body, WebDavResponse webDavResponse) {
    return writeToResponse(body,webDavResponse.withStatus(HttpResponseStatus.OK.code()));
  }

  default Future<Void> ok(WebDavResponse webDavResponse) {
    return writeToResponse(null,webDavResponse.withStatus(HttpResponseStatus.OK.code()),false);
  }


  default Future<Void> writeToResponse(String body,WebDavResponse webDavResponse,boolean chucked) {
    int length=-1;
    if(body!=null){
      length=body.length();
    }
    log.debug("response chucked[{}] body({}) {}",chucked,length,body);
    if(chucked){
      webDavResponse.getResponse().setChunked(true);
    }else {
      webDavResponse.withContentLength(Math.max(0,length));
    }
    webDavResponse.getResponse().setStatusCode(webDavResponse.ensureStatus().getStatus());
    webDavResponse.getResponse().setStatusMessage(webDavResponse.ensureMessage().getStatusMessage());
    if(body!=null){
      return webDavResponse.getResponse().end(body);
    }else {
      return webDavResponse.getResponse().end();
    }
  }

  default Future<Void> notFound(WebDavRequest webDavRequest, WebDavResponse webDavResponse) {
    return writeToResponse(page404,webDavResponse.withStatus(HttpResponseStatus.NOT_FOUND.code()));
  }

  default Future<Void> badRequest(WebDavRequest webDavRequest, WebDavResponse webDavResponse) {
    return writeToResponse(page404,webDavResponse.withStatus(HttpResponseStatus.BAD_REQUEST.code()));
  }

  default Future<Void> error(WebDavRequest webDavRequest, WebDavResponse webDavResponse,Throwable t) {
    if(t instanceof FileNotFoundException){
      webDavResponse.withStatus(HttpResponseStatus.NOT_FOUND.code());
      return notFound(webDavRequest,webDavResponse);
    }
    return Future.failedFuture(t);
  }

  default void withXmlResponse(WebDavResponse webDavResponse){
    webDavResponse.withHeader(WebDavResponseHeader.CONTENT_TYPE, WebDavResponseHeader.CONTENT_TYPE_XML_UTF8);
  }

  default void withHtmlResponse(WebDavResponse webDavResponse){
    webDavResponse.withHeader(WebDavResponseHeader.CONTENT_TYPE, WebDavResponseHeader.CONTENT_TYPE_HTML_UTF8);
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
