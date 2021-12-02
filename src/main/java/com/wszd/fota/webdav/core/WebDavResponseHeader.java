package com.wszd.fota.webdav.core;

import lombok.Data;

import java.util.HashMap;

/**
 * @author p14
 */
@Data
public class WebDavResponseHeader extends HashMap<String,String> {
  public static final String CONTENT_TYPE="Content-Type";
  public static final String CONTENT_LENGTH="Content-Length";

  public static final String CONTENT_TYPE_XML_UTF8="text/xml; charset=\"utf-8\"";
  public static final String CONTENT_TYPE_HTML_UTF8="text/html; charset=\"utf-8\"";

  public static final String CONTENT_RANGE="Content-Range";
  public static final String ACCEPT_RANGE="Accept-Range";
  public static final String LOCATION="Location";
  public static final String LOCK_TOKEN="Lock-Token";



  /**
   * HTTP/1.1 207 Multi-Status
   * Date: Sat, 27 Nov 2021 07:47:30 GMT
   * Server: Apache/2.4.46 (FreeBSD) mpm-itk/2.4.7-04 OpenSSL/1.1.1h-freebsd
   * Content-Length: 835
   * Keep-Alive: timeout=5, max=99
   * Connection: Keep-Alive
   * Content-Type: text/xml; charset="utf-8"
   */
}
