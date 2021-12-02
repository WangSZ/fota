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
public class ActiveLock {
  @XmlElement(name = "locktype")
  private LockType lockType;
  @XmlElement(name = "lockscope")
  private LockScope lockScope;
  @XmlElement(name = "depth")
  private String depth;
  @XmlElement(name = "owner")
  private Owner owner;
  @XmlElement(name = "timeout")
  private String timeout;
  @XmlElement(name = "locktoken")
  private LockToken lockToken;

  public ActiveLock() {
  }

  public ActiveLock(LockType lockType, LockScope lockScope, String depth, Owner owner, String timeout, LockToken lockToken) {
    this.lockType = lockType;
    this.lockScope = lockScope;
    this.depth = depth;
    this.owner = owner;
    this.timeout = timeout;
    this.lockToken = lockToken;
  }
}
