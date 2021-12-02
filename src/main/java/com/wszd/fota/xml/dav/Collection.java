package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Collection {
  public static final Collection EMPTY =new Collection();
}
