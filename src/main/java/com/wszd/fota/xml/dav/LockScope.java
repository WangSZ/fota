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
public class LockScope {
  public static final LockScope EXCLUSIVE=new LockScope( new Exclusive());
  public static final LockScope SHARED=new LockScope(new Shared());

  public LockScope(Exclusive exclusive) {
    this.exclusive = exclusive;
  }

  public LockScope() {
  }

  public LockScope(Shared shared) {
    this.shared = shared;
  }

  @XmlElement(name = "exclusive",nillable=true)
  private Exclusive exclusive;

  @XmlElement(name = "shared",nillable=true)
  private Shared shared;

  public static class Exclusive{

  }
  public static class Shared{

  }
}
