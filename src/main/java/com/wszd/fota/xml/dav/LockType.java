package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class LockType {
  public static final LockType WRITE=new LockType(new Write());

  public LockType() {
  }

  public LockType(Write write) {
    this.write = write;
  }



  @XmlElement(name = "write",nillable=true)
  private Write write;
  public static class Write{

  }
}
