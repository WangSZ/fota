package com.wszd.fota.util;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author p14
 */
public class DateUtil {

  public static String getStringISO8601(Date date){
    if(date==null){
      return null;
    }
    return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }
}
