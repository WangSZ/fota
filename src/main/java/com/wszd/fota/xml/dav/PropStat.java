package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PropStat<T extends Prop> {
  @XmlAnyElement
  private  T prop;
  @XmlElement(name = "status")
  private String status;

  public PropStat() {
  }

  public PropStat(T prop) {
    this.prop = prop;
  }
}
