package com.wszd.fota.xml.dav;

import com.wszd.fota.xml.win.Win32Prop;
import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author p14
 */
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Win32Prop.class})
public class Set<T extends Prop> {
  @XmlElement(name = "prop",nillable=true)
  private Win32Prop prop;
}
