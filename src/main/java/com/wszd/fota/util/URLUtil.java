package com.wszd.fota.util;

import io.netty.util.internal.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author p14
 */
public class URLUtil {
  public static String encodeUrl(String url){
    // !!! windows 下文件中的空格变成+号，因为url对空格的编码在不同的规范中有差异，这里把+还原会空格
    try {
      return URLEncoder.encode(url,"UTF-8").replaceAll("\\+"," ");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String decodeUrl(String url){
    // windows 系统传过来的+号不应该被转换成空格。 + 等于 %2B
    url=url.replaceAll("\\+","%2B");
    try {
      return URLDecoder.decode(url,"UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    System.out.println(String.format("encodeUrl '%s' '%s'","+",encodeUrl("+")));
    System.out.println(String.format("encodeUrl '%s' '%s'"," ",encodeUrl(" ")));
    //   /xxxxx 副本 + x/  /xxxxx%20%E5%89%AF%E6%9C%AC%20+%20x/ HTTP_1_1
    String fileName="/xxxxx 副本 + x/";
    String encodedByClient="/xxxxx%20%E5%89%AF%E6%9C%AC%20+%20x/";
    System.out.println(String.format("encoded  %s",encodeWholeUrl(fileName)));
    System.out.println(String.format("original %s",fileName));
    System.out.println(String.format("decode   %s",decodeUrl(encodeWholeUrl(fileName))));
    System.out.println("----");
    System.out.println(String.format("original encodedByClient %s",encodedByClient));
    System.out.println(String.format("decode   encodedByClient %s",decodeUrl(encodedByClient)));
    System.out.println(String.format("filename encodedByClient %s",fileName));
  }

  public static String encodeWholeUrl(String url){
    if(url==null){
      return null;
    }
    url=url.trim();
    String startSplash="";
    if(url.startsWith("/")){
      startSplash="/";
      url=url.substring(1);
    }
    String endSplash="";
    if(url.endsWith("/")){
      endSplash="/";
      url=url.substring(0,url.length()-1);
    }
    String newHref = Arrays.asList(url.split("/")).stream().map(s -> {
      if(StringUtil.isNullOrEmpty(s)){
        return s;
      }
      return encodeUrl(s);
    }).collect(Collectors.joining("/"));
    return startSplash+newHref+endSplash;
  }


  public static void main1(String[] args) {
    Arrays.asList(
      new String[]{"/","/"},
      new String[]{"////","////"},
      new String[]{"///","///"},
      new String[]{"/ /","/ /"},
      new String[]{"",""},
      new String[]{"a.txt","a.txt"},
      new String[]{"/a.txt","/a.txt"},
      new String[]{"/测试.txt","/%E6%B5%8B%E8%AF%95.txt"},
      new String[]{"/测试 2().txt","/%E6%B5%8B%E8%AF%95%202%28%29.txt"},
      new String[]{"/新建文件夹 -x0（） - 副本/新建文件夹 -x0()（）/","/%E6%96%B0%E5%BB%BA%E6%96%87%E4%BB%B6%E5%A4%B9%20-x0%EF%BC%88%EF%BC%89%20-%20%E5%89%AF%E6%9C%AC/%E6%96%B0%E5%BB%BA%E6%96%87%E4%BB%B6%E5%A4%B9%20-x0%28%29%EF%BC%88%EF%BC%89/"},
      new String[]{"//","//"}
    ).forEach(arr->{
      String f=encodeWholeUrl(arr[0]);
      if(!arr[1].equals(f)){
        System.out.println("error "+arr[0]+" expect "+arr[1]+" but "+f);
        System.out.println(decodeUrl(f));
      }
    });

  }

}
