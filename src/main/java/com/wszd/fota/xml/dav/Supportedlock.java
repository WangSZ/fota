package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Supportedlock {
  public static final Supportedlock DEFAULT=new Supportedlock();
  @XmlElement(name = "lockentry")
  private List<LockEntry> lockEntryList;
}
