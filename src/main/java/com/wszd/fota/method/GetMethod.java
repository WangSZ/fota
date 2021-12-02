package com.wszd.fota.method;

import com.wszd.fota.webdav.core.*;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author p14
 */
public class GetMethod implements WebDavMethod {
  @Override
  public String getMethodName() {
    return "GET";
  }

  @Override
  public void handle(WebDavEngine webDavEngine, WebDavRequest webDavRequest, WebDavResponse webDavResponse, Handler<AsyncResult<Void>> responseEndHandler) {
    IWebDavFileSystem fileSystem = getFileSystem(webDavEngine, webDavRequest);
    /**
     * 包含首尾
     * Range: bytes=0-100
     * Range: bytes=0-
     * Range: bytes=
     */
    String range = webDavRequest.getRequest().getHeader(WebDavRequestHeader.RANGE);
    fileSystem.getFileObject(webDavRequest.getWebDavPath())
      .onSuccess(fileObject -> {
        // TODO 需要设置 chucked 吗？
//        webDavResponse.getResponse().setChunked(true);
        if (StringUtil.isNullOrEmpty(range)) {
          if(fileObject.getMimeType()!=null){
            webDavResponse.setHeader(WebDavResponseHeader.CONTENT_TYPE,fileObject.getMimeType());
          }
          webDavResponse.setHeader(WebDavResponseHeader.CONTENT_LENGTH, ""+fileObject.getContentLength());
          fileSystem.readFile(webDavRequest.getVertx(), fileObject)
            .onSuccess(readStream -> readStream.pipeTo(webDavResponse.getResponse()))
            .onFailure(t -> {
              responseError(webDavResponse, responseEndHandler, t);
            });
        } else {
          // 6 是字符串 "bytes " 的长度
          List<Long> rangeList = Arrays.stream(range.trim().substring(6).split("-")).map(Long::parseLong).collect(Collectors.toList());
          long fileLength = fileObject.getContentLength();
          long begin = 0;
          long end = 0;
          if (rangeList.size() == 0) {
            end = fileLength;
          } else if (rangeList.size() == 1) {
            begin = rangeList.get(0);
            end = fileLength;
          } else if (rangeList.size() == 2) {
            begin = rangeList.get(0);
            end = rangeList.get(1);
          } else {
            responseError(webDavResponse, responseEndHandler, new IllegalArgumentException("header range must bu like Range: bytes=0-100"));
            return;
          }
          if (begin >= fileLength) {
            responseSuccess(webDavResponse, responseEndHandler);
            return;
          }
          if (end > fileLength - 1) {
            end = fileLength - 1;
          }
          if(fileObject.getMimeType()!=null){
            webDavResponse.setHeader(WebDavResponseHeader.CONTENT_TYPE,fileObject.getMimeType());
          }
          webDavResponse.setHeader(WebDavResponseHeader.ACCEPT_RANGE, "bytes");
          webDavResponse.setHeader(WebDavResponseHeader.CONTENT_RANGE, String.format("bytes %s-%s/%s", begin, end, end - begin + 1));
          webDavResponse.setHeader(WebDavResponseHeader.CONTENT_LENGTH, "" + (end - begin + 1));
          fileSystem.readFile(webDavRequest.getVertx(), fileObject,begin,end)
            .onSuccess(readStream -> readStream.pipeTo(webDavResponse.getResponse()))
            .onFailure(t -> {
              responseError(webDavResponse, responseEndHandler, t);
            });
        }
      }).onFailure(event -> {
        responseEndHandler.handle(Future.failedFuture(event));
      });
  }


}
