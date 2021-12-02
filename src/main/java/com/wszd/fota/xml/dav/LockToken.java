package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class LockToken {
  public LockToken() {
  }

  public LockToken(String href) {
    this.href = href;
  }

  public static final String OPAQUELOCK_TOKEN="opaquelocktoken";
  private String href;
}
