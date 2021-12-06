# 原罪 - 为什么又造一个轮子
文件分许多种类。
1. 按类型用：文本、压缩包、照片、音视频等。
2. 按使用频率有：常用、历史、备份、归档等。
3. 按重要程度有：废品站、临时文件、一般文件、重要文件、不能丢的文件、比命还重要的文件（狗头.jpg）等。
4. 按用途：网盘分享、工作分享、个人私藏等。

对于不同的文件，有不同的策略。像我，文件有的在本地NAS里（多块盘），有的在网盘，有的在阿里等对象存储里，有的在服务器上。入口太多，非常不方便。

我需要一个统一的入口，无论我在哪，无论我用手机、电脑还是电视，都可以看到所有的文件。最好还能方便的分享给别人。

考虑了许多方案后，就造了这么个轮子 FOTA(File on the air) 。
```
手机、电脑、电视 -> WebDAV -> NAS 0..1 | S3存储 | 网盘 | ...
```
WebDAV 协议非常古老，可能已经不适应现在的移动互联网了，但它足够简单，且兼容性非常好（虽然不适合高频读写）。用 WebDAV 做一个中间人，其它所有的文件都通过这个中间人来存取。


# FOTA - File on the air
## 开发

构建项目:
```shell
./mvnw -DskipTests=true clean package
```
启动项目:
```shell
./mvnw -DskipTests=true clean compile exec:java
```
启动命令：（默认用户名密码: fota fota）
```shell
java -jar target/fota-fat.jar
java -jar fota-fat.jar -conf /mnt/fota.json
```
配置文件例子1【挂载单个本地文件目录】
```json
{
  "webdav.root": "/webdav",
  "webdav.port": 9999,
  "username": "fota",
  "password": "fota",
  "default.fileSystem.class": "com.wszd.fota.webdav.filesystem.LocalFileSystem",
  "default.fileSystem.config.root": "."
}
```
配置文件例子2【挂载多个本地文件目录】
```json
{
  "webdav.root": "/webdav",
  "webdav.port": 9999,
  "username": "fota",
  "password": "fota",
  "default.fileSystem.class": "com.wszd.fota.webdav.filesystem.UnionWebDavFileSystem",
  "default.fileSystem.config.name": "union-x",
  "default.fileSystem.config.worker.name": "bio-foo",
  "default.fileSystem.config.worker.size": "100",
  "default.fileSystem.config.include": "f1,f2,f3",
  "default.fileSystem.config.f1.class": "com.wszd.fota.webdav.filesystem.LocalFileSystem",
  "default.fileSystem.config.f1.path": "/abc",
  "default.fileSystem.config.f1.config.name": "f1-foo",
  "default.fileSystem.config.f1.config.root": "d:/tmp",
  "default.fileSystem.config.f1.config.worker.name": "f1-bil",
  "default.fileSystem.config.f1.config.worker.size": "100",
  "default.fileSystem.config.f2.class": "com.wszd.fota.webdav.filesystem.LocalFileSystem",
  "default.fileSystem.config.f2.path": "/def",
  "default.fileSystem.config.f2.config.name": "f1-bar",
  "default.fileSystem.config.f2.config.root": "e:/tmp",
  "default.fileSystem.config.f2.config.worker.name": "f1-bil",
  "default.fileSystem.config.f2.config.worker.size": "100",
  "default.fileSystem.config.f3.class": "com.wszd.fota.webdav.filesystem.LocalFileSystem",
  "default.fileSystem.config.f3.path": "/下载",
  "default.fileSystem.config.f3.config.name": "f1-bar",
  "default.fileSystem.config.f3.config.root": "e:/download",
  "default.fileSystem.config.f3.config.worker.name": "f1-bil",
  "default.fileSystem.config.f3.config.worker.size": "100"
}

```

## Roadmap
1. 【Done】RaiDrive 的增删改查、断点续传等基础功能。目标：验证协议基本内容。
2. 【Testing】多目录聚合。目标：实现多个目录或者盘符聚合到一起。
3. 【】增加回收站，定期清理。目标：防止手贱误删。
4. 【Done】Windows 映射的增删改查。目标：兼容 windows 自带映射功能。
5. 【Done】Base path支持。目标：自定义网站的base path，以适应其它 client。
6. 【Done】浏览器查看。目标：可通过浏览器查看和下载文件。
7. 【Done】浏览器 的增删改查。目标：实现简单的网盘。 用开源 filebrowser
8. 【】支持 Android/ios 下 U-File、ES 等 的增删改查。目标：手机、电脑、浏览器内容一致
9. 【】支持 Webdav、s3、ftp(s) 多协议聚合。目标：管理个人所有云盘和本地Nas文件
10. 【Done】Basic、digest 验证及鉴权。
11. 【】实现不同用户登录后，可查看不同的内容。目标：实现基本的安全和权限管理
12. 【】增强安全功能。目标：确保外网可安全访问，包含审计、限流、异常告警等
