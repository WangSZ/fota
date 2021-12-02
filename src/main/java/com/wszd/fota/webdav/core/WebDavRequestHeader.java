package com.wszd.fota.webdav.core;

import lombok.Data;

import java.util.HashMap;

/**
 * @author p14
 */
@Data
public class WebDavRequestHeader extends HashMap<String,String> {
  public final static String DEPTH="Depth";
  public final static String TRANSLATE="translate";
  public final static String USER_AGENT="User-Agent";
  public final static String HOST="Host";
  public final static String CONTENT_LENGTH="Content-Length";
  public final static String PRAGMA="Pragma";
  public final static String CACHE_CONTROL="Cache-Control";
  public static final String RANGE="Range";
  public static final String OVERWRITE="Overwrite";
  public static final String OVERWRITE_T="T";
  public static final String OVERWRITE_F="F";
  public static final String DESTINATION="Destination";
  /**
   * PROPFIND http://172.17.1.147:8080/webdav HTTP/1.1
   * Connection: Keep-Alive
   * User-Agent: Microsoft-WebDAV-MiniRedir/10.0.19043
   * Depth: 0
   * translate: f
   * Content-Length: 0
   * Host: 172.17.1.147:8080
   *
   * GET http://172.17.1.147:8080/webdav/test-1/a.txt HTTP/1.1
   * Cache-Control: no-cache
   * Connection: Keep-Alive
   * Pragma: no-cache
   * User-Agent: Microsoft-WebDAV-MiniRedir/10.0.19043
   * translate: f
   * Host: 172.17.1.147:8080
   */
}
