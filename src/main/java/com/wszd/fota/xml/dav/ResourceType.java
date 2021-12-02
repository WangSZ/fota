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
public class ResourceType {
  public static final ResourceType COLLECTION=new ResourceType(new Collection());
  @XmlElement
  private Collection collection;

  public ResourceType() {
  }

  public ResourceType(Collection collection) {
    this.collection = collection;
  }
}
