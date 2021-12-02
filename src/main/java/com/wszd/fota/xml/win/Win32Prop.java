
package com.wszd.fota.xml.win;

import com.wszd.fota.xml.dav.Prop;
import lombok.Data;
import lombok.ToString;

import javax.xml.bind.annotation.*;

/**
 * @author p14
 */
@Data
@XmlRootElement(name = "prop",namespace = "DAV:")
@XmlAccessorType(XmlAccessType.FIELD)
@ToString(callSuper = true)
public class Win32Prop extends Prop {
  @XmlElement(name = "Win32CreationTime",nillable=true)
  private String win32CreationTime;
  @XmlElement(name = "Win32LastAccessTime",nillable=true)
  private String win32LastAccessTime;
  @XmlElement(name = "Win32LastModifiedTime",nillable=true)
  private String win32LastModifiedTime;
  @XmlElement(name = "Win32FileAttributes",nillable=true)
  private String win32FileAttributes;

}
