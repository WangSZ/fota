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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Propertyupdate {
  @XmlElement(name = "set")
  private Set set;
}
