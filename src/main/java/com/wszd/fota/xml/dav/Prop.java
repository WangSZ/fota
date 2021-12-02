package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author p14
 *
 *
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Prop {

  @XmlElement(name = "displayname")
  private String displayName;
  @XmlElement(name = "resourcetype")
  private ResourceType resourceType;
  @XmlElement(name = "creationdate")
  private String creationDate;
  @XmlElement(name = "getcontenttype")
  private String contentType;
  @XmlElement(name = "getcontentlength")
  private Long contentLength;
  @XmlElement(name = "getlastmodified")
  private String lastModified;
  @XmlElement(name = "getetag")
  private String eTag;
  @XmlElement(name = "supportedlock")
  private Supportedlock supportedlock;
  @XmlElement(name = "lockdiscovery")
  private LockDiscovery lockDiscovery;

}
