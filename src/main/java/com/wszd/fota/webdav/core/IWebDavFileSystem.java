package com.wszd.fota.webdav.core;

import com.wszd.fota.xml.dav.Propertyupdate;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

import java.util.List;

/**
 * @author p14
 */
public interface IWebDavFileSystem {
  /**
   * 返回唯一的名字
   * @return
   */
  String name();
  /**
   * 初始化文件系统，包括创建目录、连接等。
   * @param vertx
   * @return
   */
  Future<Void> init(Vertx vertx, JsonObject config,WebDavFileSystemRegistry registry);

  /**
   * 获取 FileObject 对象
   * @param webDavPath
   * @return
   */
  Future<FileObject> getFileObject(String webDavPath);

  /**
   * 查询指定目录及子目录
   * @param webDavPath
   * @param depth
   * @return
   */
  Future<List<FileObject>> listFile(String webDavPath, int depth);

  /**
   * 读取指定文件
   * @param vertx
   * @param fileObject
   * @return
   */
  default Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject){
    return readFile(vertx,fileObject,0, fileObject.getContentLength());
  }

  /**
   * 读取指定文件的一部分
   * @param vertx
   * @param fileObject
   * @param begin
   * @param end
   * @return
   */
  Future<ReadStream<Buffer>> readFile(Vertx vertx, FileObject fileObject, long begin, long end);

  /**
   * 更新文件内容。如果没有则创建
   * @param vertx
   * @param webDavPath
   * @param buffer
   * @return
   */
  Future<Void> updateOrCreateFile(Vertx vertx, String webDavPath, Buffer buffer);

  /**
   * 删除单个文件
   * @param vertx
   * @param webDavPath
   * @return
   */
  Future<Void> deleteFile(Vertx vertx, String webDavPath);

  /**
   * 删除指定目录
   * @param vertx
   * @param webDavPath
   * @param recursive
   * @return
   */
  Future<Void> deleteDirectory(Vertx vertx, String webDavPath, boolean recursive);

  /**
   * 移动文件到指定目录
   * @param vertx
   * @param webDavPath
   * @param targetLocation
   * @param overwrite
   * @return
   */
  Future<Void> moveFile(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite);

  /**
   * 重命名目录，等于移动目录
   * @param vertx
   * @param webDavPath
   * @param targetLocation
   * @param overwrite
   * @return
   */
  Future<Void> moveDirectory(Vertx vertx, String webDavPath, String targetLocation, boolean overwrite);

  /**
   * 创建目录
   * @param vertx
   * @param webDavPath
   * @return
   */
  Future<Void> makeDirectory(Vertx vertx, String webDavPath);

  /**
   * 更新文件属性
   * @param vertx
   * @param webDavPath
   * @param propertyupdate
   * @return
   */
  Future<Void> updateProperties(Vertx vertx, String webDavPath, Propertyupdate propertyupdate);

}
