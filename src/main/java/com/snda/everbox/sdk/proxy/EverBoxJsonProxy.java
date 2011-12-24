package com.snda.everbox.sdk.proxy;

import com.snda.everbox.config.Config;
import com.snda.everbox.fs.UploadProgressListener;
import com.snda.everbox.sdk.auth.Auth;
import com.snda.everbox.utils.ELog;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class EverBoxJsonProxy extends EverBoxProxyBase {
	public String CallGet(String paramString1, JSONObject paramJSONObject,
			String paramString2) throws Exception {
		StringBuilder localStringBuilder = new StringBuilder().append(
				paramString1).append("?");
		String str1 = paramJSONObject.toString();
		String str2 = str1;
		HttpGet localHttpGet = new HttpGet(str2);
		String str3 = Config.getUserAgent();
		localHttpGet.setHeader("User-Agent", str3);
		HttpRequest localHttpRequest = Auth.getConsumer().sign(localHttpGet);
		DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
		HttpParams localHttpParams1 = localDefaultHttpClient.getParams();
		Integer localInteger = Integer.valueOf(10000);
		HttpParams localHttpParams2 = localHttpParams1.setParameter(
				"http.connection.timeout", localInteger);
		HttpResponse localHttpResponse = localDefaultHttpClient
				.execute(localHttpGet);
		String str4 = "";
		if (localHttpResponse.getStatusLine().getStatusCode() == 200)
			str4 = EntityUtils.toString(localHttpResponse.getEntity());
		return str4;
	}

	public byte[] CallGetBytes(String paramString) throws Exception {
		HttpGet localHttpGet = new HttpGet(paramString);
		String str = Config.getUserAgent();
		localHttpGet.setHeader("User-Agent", str);
		DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
		HttpParams localHttpParams1 = localDefaultHttpClient.getParams();
		Integer localInteger = Integer.valueOf(10000);
		HttpParams localHttpParams2 = localHttpParams1.setParameter(
				"http.connection.timeout", localInteger);
		HttpResponse localHttpResponse = localDefaultHttpClient
				.execute(localHttpGet);
		byte[] arrayOfByte = null;
		if (localHttpResponse.getStatusLine().getStatusCode() == 200)
			arrayOfByte = EntityUtils
					.toByteArray(localHttpResponse.getEntity());
		return arrayOfByte;
	}

	public byte[] CallGetBytes(String paramString1, String paramString2,
			String paramString3) throws Exception {
		String str1 = paramString1 + "?" + paramString2;
		HttpGet localHttpGet = new HttpGet(str1);
		String str2 = Config.getUserAgent();
		localHttpGet.setHeader("User-Agent", str2);
		DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
		HttpParams localHttpParams1 = localDefaultHttpClient.getParams();
		Integer localInteger = Integer.valueOf(10000);
		HttpParams localHttpParams2 = localHttpParams1.setParameter(
				"http.connection.timeout", localInteger);
		HttpResponse localHttpResponse = localDefaultHttpClient
				.execute(localHttpGet);
		byte[] arrayOfByte = null;
		if (localHttpResponse.getStatusLine().getStatusCode() == 200)
			arrayOfByte = EntityUtils
					.toByteArray(localHttpResponse.getEntity());
		return arrayOfByte;
	}

	public int CallPostFileStream(Object paramProgressTask, String paramString,
			RandomAccessFile paramRandomAccessFile, int paramInt,
			StringBuilder paramStringBuilder,
			UploadProgressListener paramUploadProgressListener)
			throws Exception {
		return 1;
	}

	public int CallPostFileStream(String paramString,
			RandomAccessFile paramRandomAccessFile, int paramInt,
			StringBuilder paramStringBuilder,
			UploadProgressListener paramUploadProgressListener)
			throws Exception {
		EverBoxJsonProxy localEverBoxJsonProxy = this;
		String str = paramString;
		RandomAccessFile localRandomAccessFile = paramRandomAccessFile;
		int i = paramInt;
		StringBuilder localStringBuilder = paramStringBuilder;
		UploadProgressListener localUploadProgressListener = paramUploadProgressListener;
		return localEverBoxJsonProxy.CallPostFileStream(null, str,
				localRandomAccessFile, i, localStringBuilder,
				localUploadProgressListener);
	}

	@Override
	public int CallPost(String paramString1, JSONObject paramJSONObject,
			String paramString2, StringBuilder paramStringBuilder)
			throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int CallPostBytes(String paramString, byte[] paramArrayOfByte,
			StringBuilder paramStringBuilder,
			UploadProgressListener paramUploadProgressListener)
			throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String CallPostBytes(String paramString1, String paramString2,
			File paramFile, String paramString3) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

/*
 * Location: G:\xampp\htdocs\everbox-0.1.0.jar Qualified Name:
 * com.snda.everbox.sdk.proxy.EverBoxJsonProxy JD-Core Version: 0.5.4
 */