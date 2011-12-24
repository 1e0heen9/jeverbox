package com.snda.everbox.sdk.fs;

import com.snda.everbox.sdk.proxy.EverBoxJsonProxy;
import com.snda.everbox.sdk.url.EverBoxServiceURLs;
import org.json.JSONObject;

public class FS
{
  public static int commitPut(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/commit_put";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int get(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/get";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int list(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/list";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int preparePut(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/prepare_put";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int put(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/put";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int query(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/query";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }

  public static int thumbnail(JSONObject paramJSONObject, StringBuilder paramStringBuilder)
    throws Exception
  {
    EverBoxJsonProxy localEverBoxJsonProxy = new EverBoxJsonProxy();
    StringBuilder localStringBuilder = new StringBuilder();
    String str1 = EverBoxServiceURLs.META_SERVER;
    String str2 = str1 + "/thumbnail";
    return localEverBoxJsonProxy.CallPost(str2, paramJSONObject, "UTF-8", paramStringBuilder);
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.sdk.fs.FS
 * JD-Core Version:    0.5.4
 */