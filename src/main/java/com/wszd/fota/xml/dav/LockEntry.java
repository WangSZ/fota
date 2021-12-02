package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class LockEntry {
  public static final LockEntry EXCLUSIVE_WRITE=new LockEntry(LockScope.EXCLUSIVE,LockType.WRITE);
  public static final LockEntry SHARED_WRITE=new LockEntry(LockScope.SHARED,LockType.WRITE);
  public static final List<LockEntry> DEFAULT_ENTRY_LIST= Arrays.asList(EXCLUSIVE_WRITE,SHARED_WRITE);
  @XmlElement(name = "lockscope")
  private LockScope lockScope;
  @XmlElement(name = "locktype")
  private LockType lockType;

  public LockEntry(LockType lockType) {
    this.lockType = lockType;
  }

  public LockEntry() {
  }

  public LockEntry(LockScope lockScope) {
    this.lockScope = lockScope;
  }

  public LockEntry(LockScope lockScope, LockType lockType) {
    this.lockScope = lockScope;
    this.lockType = lockType;
  }
}
