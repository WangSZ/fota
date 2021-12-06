package com.wszd.fota.webdav;

/**
 * @author p14
 */
public class FileNotFoundException extends RuntimeException{
  public FileNotFoundException(String file) {
    super("FileNotFound "+file);
  }
}
