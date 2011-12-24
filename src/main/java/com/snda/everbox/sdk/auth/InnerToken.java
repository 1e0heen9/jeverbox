package com.snda.everbox.sdk.auth;

public class InnerToken
{
  private String secret;
  private String token;

  public InnerToken(String paramString1, String paramString2)
  {
    this.token = paramString1;
    this.secret = paramString2;
  }

  public String getSecret()
  {
    return this.secret;
  }

  public String getToken()
  {
    return this.token;
  }

  public void setSecret(String paramString)
  {
    this.secret = paramString;
  }

  public void setToken(String paramString)
  {
    this.token = paramString;
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.sdk.auth.InnerToken
 * JD-Core Version:    0.5.4
 */