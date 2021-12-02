package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author p14
 */
@Data
@XmlRootElement(name = "lockinfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class LockInfo {
  @XmlElement(name = "lockscope",nillable=true)
  private LockScope lockScope;
  @XmlElement(name = "locktype",nillable=true)
  private LockType lockType;
  @XmlElement(name = "owner",nillable=true)
  private Owner owner;
}
