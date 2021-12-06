package com.wszd.fota.xml.dav;


import com.wszd.fota.util.DateUtil;
import com.wszd.fota.xml.win.Win32Prop;
import io.vertx.core.impl.ConcurrentHashSet;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.bind.Marshaller.JAXB_FRAGMENT;

/**
 * @author p14
 */
public class XmlUtil {
  public static final ConcurrentHashSet<Class> loadClass = new ConcurrentHashSet<>();
  public static final ConcurrentHashMap<List<Class>, JAXBContext> jaxbContextCache = new ConcurrentHashMap<>();

  public static final JAXBContext getOrCreate(Class... load) {
    return jaxbContextCache.computeIfAbsent(Arrays.asList(load), arr -> {
      try {
        return JAXBContext.newInstance(load);
      } catch (JAXBException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static void main(String[] args) {
    t3();
  }
  public static final void t4(){
    String xml="<!DOCTYPE a [\n" +
      "<!ENTITY % name SYSTEM “file:///etc/passwd”>\n" +
      "%name;\n" +
      "]>";
    System.out.println();
  }

  public static void t3() {
    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\">  <locktype><write/></locktype>  <lockscope><exclusive/></lockscope>  <owner><href>anonymous</href></owner></lockinfo>";
    System.out.println(xmlToObject(xml, LockInfo.class));
  }

  private static void t1() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.setResponseList(new ArrayList<>());
    multiStatus.getResponseList().add(new Response());
    multiStatus.getResponseList().get(0).setHref("/");
    PropStat propStat = new PropStat();
    propStat.setStatus("HTTP/1.1 200 OK");
    Win32Prop p = new Win32Prop();
    p.setWin32CreationTime("xxx");
    p.setWin32FileAttributes("112");
    Prop p2 = new Prop();
    propStat.setProp(p2);
    propStat.getProp().setResourceType(new ResourceType());
    propStat.getProp().getResourceType().setCollection(Collection.EMPTY);
    propStat.getProp().setCreationDate(DateUtil.getStringISO8601(new Date()));
    propStat.getProp().setLastModified(new Date().toString());
    propStat.getProp().setETag("etttt");
    propStat.getProp().setContentLength(0L);
    propStat.getProp().setContentType("directory");
    propStat.getProp().setDisplayName("demo-name");

    Supportedlock lock = new Supportedlock();
    propStat.getProp().setSupportedlock(lock);
//    propStat.getProp().set
    multiStatus.getResponseList().get(0).setPropStat(propStat);

    System.out.println(objectToXml(multiStatus, Win32Prop.class));

  }

  private static void t2() {
    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
      "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:Z=\"urn:schemas-microsoft-com:\">\n" +
      "  <D:set>\n" +
      "    <D:prop>\n" +
      "      <Z:Win32CreationTime>Mon, 29 Nov 2021 13:20:26 GMT</Z:Win32CreationTime>\n" +
      "      <D:getlastmodified>Mon, 29 Nov 2021 13:20:26 GMT</D:getlastmodified>\n" +
      "      <Z:Win32LastAccessTime>Mon, 29 Nov 2021 13:20:26 GMT</Z:Win32LastAccessTime>\n" +
      "      <Z:Win32LastModifiedTime>Mon, 29 Nov 2021 13:20:26 GMT</Z:Win32LastModifiedTime>\n" +
      "      <Z:Win32FileAttributes>00000020</Z:Win32FileAttributes>\n" +
      "    </D:prop>\n" +
      "  </D:set>\n" +
      "</D:propertyupdate>";
    Object obj = xmlToObject(xml, Propertyupdate.class);
    System.out.println(obj);
  }

  public static String objectToXml(Object obj, Class... load) {
    try {
      ArrayList<Class> classes = new ArrayList<>();
      classes.add(obj.getClass());
      for (int i = 0; i < load.length; i++) {
        classes.add(load[i]);
      }
      JAXBContext context = getOrCreate(classes.toArray(load));
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(JAXB_FRAGMENT, false);
      StringWriter writer = new StringWriter();
      marshaller.marshal(obj, writer);
      return writer.toString();
    } catch (JAXBException e) {
      throw new RuntimeException("error to marshall xml", e);
    }
  }

  public static Object xmlToObject(String xml, Class... load) {
    try {
      JAXBContext context = getOrCreate(load);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      XMLInputFactory xif = XMLInputFactory.newFactory();
      // 防止xml外部实体注入攻击
      xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));
      Object object = unmarshaller.unmarshal(xsr);
      return object;
    } catch (JAXBException | XMLStreamException e) {
      throw new RuntimeException("error to unmarshal xml", e);
    }
  }


}
