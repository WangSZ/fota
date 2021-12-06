package com.wszd.fota.method;

import com.wszd.fota.webdav.core.*;
import com.wszd.fota.xml.dav.Response;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author p14
 */
@Slf4j
public class GetMethod  implements WebDavMethod{
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
      .compose(f -> {
        if(!f.isExist()){
          return notFound(webDavRequest,webDavResponse);
        }
        if(f.isDirectory()){
          StringBuilder body=new StringBuilder();
          return fileSystem.listFile(f.getWebDavPath(),1).compose(list->{
            body.append("<html>");
            body.append("<head>");
            body.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>");
            body.append("<title>FOTA Index of ").append(f.getWebDavPath()).append("</title>");
            body.append("</head>");
            body.append("<body>");
            body.append("<h1>Index of ").append(f.getWebDavPath()).append("</h1>");
            body.append("<ul>");
            body.append("<li><a href=\"").append("../").append("\"> Parent Directory</a></li>");
            list.forEach(file->{
              if(file.getWebDavPath().equals(f.getWebDavPath())){
                // 跳过当前目录
                return;
              }
              body.append("<li><a href=\"").append(Response.encodeHref(webDavResponse.getWebDavRoot(),file.getWebDavPath())).append("\"> <span>").append(file.getDisplayName()).append("</span></a></li>");
            });
            body.append("</ul>");
            body.append("</body>");
            body.append("</html>");
            withHtmlResponse(webDavResponse);
            return writeToResponse(body.toString(),webDavResponse.withStatus(HttpResponseStatus.OK.code()),true);
          });
        }else {
          if (StringUtil.isNullOrEmpty(range)) {
            if(f.getMimeType()!=null){
              String type=f.getMimeType();
              if(type.contains("text")&&!type.contains("charset")){
                type=type+"; charset=utf-8";
              }
              webDavResponse.withHeader(WebDavResponseHeader.CONTENT_TYPE,type);
            }
            webDavResponse.withHeader(WebDavResponseHeader.CONTENT_LENGTH, ""+f.getContentLength());
            return fileSystem.readFile(webDavRequest.getVertx(), f).compose(readStream -> readStream.pipeTo(webDavResponse.getResponse()));
          } else {
            // 6 是字符串 "bytes " 的长度
            List<Long> rangeList = Arrays.stream(range.trim().substring(6).split("-")).map(Long::parseLong).collect(Collectors.toList());
            long fileLength = f.getContentLength();
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
              return error(webDavRequest,webDavResponse,new IllegalArgumentException("header range must bu like Range: bytes=0-100"));
            }
            if (begin >= fileLength) {
              return writeOKToResponse(null,webDavResponse);
            }
            if (end > fileLength - 1) {
              end = fileLength - 1;
            }
            if(f.getMimeType()!=null){
              webDavResponse.withHeader(WebDavResponseHeader.CONTENT_TYPE,f.getMimeType());
            }
            webDavResponse.withHeader(WebDavResponseHeader.ACCEPT_RANGE, "bytes");
            webDavResponse.withHeader(WebDavResponseHeader.CONTENT_RANGE, String.format("bytes %s-%s/%s", begin, end, end - begin + 1));
            webDavResponse.withHeader(WebDavResponseHeader.CONTENT_LENGTH, "" + (end - begin + 1));
            return fileSystem.readFile(webDavRequest.getVertx(), f,begin,end)
              .compose(readStream -> readStream.pipeTo(webDavResponse.getResponse()));
          }
        }
      }).onComplete(responseEndHandler);
  }

}
