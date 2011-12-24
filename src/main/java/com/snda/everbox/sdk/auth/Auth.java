package com.snda.everbox.sdk.auth;

import com.snda.everbox.utils.ELog;
import java.io.IOException;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class Auth
{
  private static InnerToken accessToken = null;

  public static InnerToken getAccessToken()
  {
    return accessToken;
  }

  public static OAuthConsumer getConsumer()
  {
    if (accessToken == null)
      ELog.e("there are no accessToken!");
    OAuthConsumer localOAuthConsumer;
    for (Object localObject = null; ; localObject = localOAuthConsumer)
    {
      localOAuthConsumer = getRawConsumer();
      String str1 = accessToken.getToken();
      String str2 = accessToken.getSecret();
      localOAuthConsumer.setTokenWithSecret(str1, str2);
    }
  }

  private static OAuthConsumer getRawConsumer()
  {
    return new CommonsHttpOAuthConsumer("QuGMi6ldAUsJqD3C6Wt27jCAGdAd95mEvz6GzGlf", "wG2LN4i6kQ0PqNbbaRGlSvWWCSavAMc75LbLUO61");
  }

  public static int quickToken(String paramString)
    throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, ClientProtocolException, IOException
  {
    DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
    HttpPost localHttpPost = new HttpPost(paramString);
    HttpRequest localHttpRequest = getRawConsumer().sign(localHttpPost);
    HttpParams localHttpParams1 = localDefaultHttpClient.getParams();
    Integer localInteger = Integer.valueOf(10000);
    HttpParams localHttpParams2 = localHttpParams1.setParameter("http.connection.timeout", localInteger);
    HttpResponse localHttpResponse = localDefaultHttpClient.execute(localHttpPost);
    int i = localHttpResponse.getStatusLine().getStatusCode();
    if (i != 200)
      ELog.i("failed to get quick token, errno:" + i);
    while (true)
    {
      HttpParameters localHttpParameters = OAuth.decodeForm(EntityUtils.toString(localHttpResponse.getEntity()));
      String str1 = localHttpParameters.getFirst("oauth_token");
      String str2 = localHttpParameters.getFirst("oauth_token_secret");
      accessToken = new InnerToken(str1, str2);
    }
  }

  public static void setAccessToken(InnerToken paramInnerToken)
  {
    accessToken = paramInnerToken;
  }

  public static void setAccessToken(String paramString1, String paramString2)
  {
    accessToken = new InnerToken(paramString1, paramString2);
  }
}

/* Location:           G:\xampp\htdocs\everbox-0.1.0.jar
 * Qualified Name:     com.snda.everbox.sdk.auth.Auth
 * JD-Core Version:    0.5.4
 */