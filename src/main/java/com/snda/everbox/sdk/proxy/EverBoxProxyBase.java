package com.snda.everbox.sdk.proxy;

import com.snda.everbox.fs.UploadProgressListener;
import java.io.File;
import org.json.JSONObject;

public abstract class EverBoxProxyBase
{
  public abstract String CallGet(String paramString1, JSONObject paramJSONObject, String paramString2)
    throws Exception;

  public abstract byte[] CallGetBytes(String paramString)
    throws Exception;

  public abstract byte[] CallGetBytes(String paramString1, String paramString2, String paramString3)
    throws Exception;

  public abstract int CallPost(String paramString1, JSONObject paramJSONObject, String paramString2, StringBuilder paramStringBuilder)
    throws Exception;

  public abstract int CallPostBytes(String paramString, byte[] paramArrayOfByte, StringBuilder paramStringBuilder, UploadProgressListener paramUploadProgressListener)
    throws Exception;

  public abstract String CallPostBytes(String paramString1, String paramString2, File paramFile, String paramString3)
    throws Exception;
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.sdk.proxy.EverBoxProxyBase
 * JD-Core Version:    0.5.4
 */