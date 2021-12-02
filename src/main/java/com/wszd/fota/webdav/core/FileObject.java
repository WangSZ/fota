package com.wszd.fota.webdav.core;

import io.vertx.core.http.impl.MimeMapping;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.Date;
import java.util.Optional;

/**
 * @author p14
 */
@Data
@Slf4j
public class FileObject {
  private String webDavPath;
  private boolean directory;
  private Date lastModified;
  private Date creationDate;
  private Date lastAccess;
  private long contentLength;
  private String contentType;
  private String mimeType;
  private String fileAttrs;
  private boolean exist;

  /**
   * window 系统文件属性
   */
  private Date win32CreationTime;
  private Date win32LastAccessTime;
  private Date win32LastModifiedTime;
  private String win32FileAttributes;

  public static FileObject newObjectFromFile(String webDavPath,File file){
    FileObject fileObject=new FileObject();
    fileObject.setWebDavPath(webDavPath);
    fileObject.setExist(file.exists());
    try {
      fileObject.setMimeType(MimeMapping.getMimeTypeForFilename(file.getAbsolutePath()));
      fileObject.setContentType(Files.probeContentType(file.toPath()));
    } catch (IOException e) {
      fileObject.setContentType("");
    }
    if(fileObject.isExist()){
      fileObject.setDirectory(file.isDirectory());
      fileObject.setLastModified(new Date(file.lastModified()));
      fileObject.setCreationDate(new Date(file.lastModified()));
      fileObject.setContentLength(file.length());
      try {
        Optional.ofNullable(Files.readAttributes(file.toPath(), BasicFileAttributes.class)).ifPresent(basicFileAttributes -> {

          if(basicFileAttributes.lastAccessTime()!=null){
            fileObject.setLastAccess(Date.from(basicFileAttributes.lastAccessTime().toInstant()));
          }
          if(basicFileAttributes instanceof DosFileAttributes){
            // WindowsFileAttributes
            fileObject.setWin32FileAttributes("");
            fileObject.setWin32CreationTime(fileObject.getCreationDate());
            fileObject.setWin32LastModifiedTime(fileObject.getLastModified());
            fileObject.setWin32LastAccessTime(fileObject.getLastAccess());
          }
        });
      } catch (IOException ignore) {
      }
    }
    return fileObject;
  }

}
