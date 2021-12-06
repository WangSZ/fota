package com.wszd.fota.xml.dav;

import com.wszd.fota.util.URLUtil;
import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author p14
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class Response {

  @XmlElement(name = "href")
  private String href;
  @XmlElement(name = "propstat" )
  private PropStat propStat;

  public static final String encodeHref(String rootPath,String href){
    return URLUtil.encodeWholeUrl(rootPath+href);
  }

}
