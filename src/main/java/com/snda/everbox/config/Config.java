package com.snda.everbox.config;

import com.snda.everbox.utils.ELog;
import java.io.File;

public class Config
{
  public static int screenHeight;
  public static int screenWidth;
  public static String sdcardPath;
  public static String storagePath = "";
  public static String userAgent;
  public static String userCachePath;
  public static String userPath;
  public static String userTaskPath;
  public static String userTempPath;
  public static String version;

  static
  {
    sdcardPath = "";
    userPath = "";
    userTempPath = "";
    userTaskPath = "";
    userCachePath = "";
    userAgent = null;
    version = "0.0.0";
    screenWidth = 0;
    screenHeight = 0;
  }

  public static void createDirs()
  {
	  //TODO
  }

  public static String getImageCachePath()
  {
    return userCachePath;
  }

  public static String getLocalFileRootPath()
  {
    return storagePath;
  }

  public static String getOriginalPicPath()
  {
    return userTaskPath;
  }

  public static String getSDCardPath()
  {
    return sdcardPath;
  }

  public static String getTempDir()
  {
    return userTempPath;
  }

  public static String getTempPicPath()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    String str = userTempPath;
    return str + "/" + "temp.bmp";
  }

  public static String getUserAgent()
  {
    if (userAgent == null)
    {
      StringBuilder localStringBuilder1 = new StringBuilder();
      StringBuilder localStringBuilder2 = localStringBuilder1.append("everbox-android/");
      String str1 = version;
      StringBuilder localStringBuilder3 = localStringBuilder1.append(str1);
      StringBuilder localStringBuilder4 = localStringBuilder1.append(" (");
      StringBuilder localStringBuilder5 = localStringBuilder1.append("s:");
      int i = screenWidth;
      StringBuilder localStringBuilder6 = localStringBuilder5.append(i).append("x");
      int j = screenHeight;
      StringBuilder localStringBuilder7 = localStringBuilder6.append(j);
      StringBuilder localStringBuilder8 = localStringBuilder1.append(")");
      userAgent = localStringBuilder1.toString();
      StringBuilder localStringBuilder9 = new StringBuilder().append("user agent:");
      String str2 = userAgent;
      ELog.i(str2);
    }
    return userAgent;
  }

  public static String getUserPath()
  {
    return userPath;
  }

  public static void setScreenWH(int paramInt1, int paramInt2)
  {
    screenWidth = paramInt1;
    screenHeight = paramInt2;
  }

  public static void setVersion(String paramString)
  {
    version = paramString;
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.config.Config
 * JD-Core Version:    0.5.4
 */