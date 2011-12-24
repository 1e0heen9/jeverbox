package com.snda.everbox.sdk.account;

import com.snda.everbox.sdk.auth.Auth;
import com.snda.everbox.sdk.url.EverBoxServiceURLs;

public class Account
{
  public static int login(String paramString1, String paramString2)
    throws Exception
  {
    StringBuilder localStringBuilder1 = new StringBuilder();
    String str1 = EverBoxServiceURLs.ACCOUNT_SERVER;
    StringBuilder localStringBuilder2 = localStringBuilder1.append(str1).append("/oauth/quick_token");
    Object[] arrayOfObject = new Object[3];
    arrayOfObject[0] = paramString1;
    arrayOfObject[1] = paramString2;
    arrayOfObject[2] = "sdo";
    String str2 = String.format("?login=%s&password=%s&provider=%s", arrayOfObject);
    return Auth.quickToken(str2);
  }

  public static int ping()
    throws Exception
  {
    //TODO
	return 0;
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.sdk.account.Account
 * JD-Core Version:    0.5.4
 */