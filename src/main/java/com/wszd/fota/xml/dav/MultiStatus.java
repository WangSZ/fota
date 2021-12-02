package com.wszd.fota.xml.dav;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author p14
 */
@Data
@XmlRootElement(name = "multistatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiStatus {
  @XmlElement(name = "response")
  private List<Response> responseList;

}
