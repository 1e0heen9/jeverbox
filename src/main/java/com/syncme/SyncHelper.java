package com.syncme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.EverboxConfig;
import com.jeverbox.bean.EverBoxObject;
import com.snda.everbox.consts.Constants;

public class SyncHelper {
	
	private static final Log log = Logs.get();

	public static String[] makeFileKeys(EverBoxObject ebo) {
		try {
			File f = new File(EverboxConfig.getRealPath(ebo));
			if(!f.exists())
				return null;
			if(!f.isFile())
				return null;
			List<String> keys = new ArrayList<String>();
			long ls = f.length();
			int part = (int)(f.length() / Constants.FILE_CHUNK_SIZE) + 1;
			log.infof("将文件分成%d段,进行分析",part);
			FileInputStream is = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(is, 8196);
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			for (int i = 0; i < part; i++) {
				byte[] sha = null;
				if(ls > Constants.FILE_CHUNK_SIZE) {
					sha = SHA1(sha1, bis, Constants.FILE_CHUNK_SIZE);
					ls = ls - Constants.FILE_CHUNK_SIZE;
				} else {
					sha = SHA1(sha1, bis, (int)ls);
				}
				String key = Base64.encodeBase64String(sha).replace('/', '_');
				key = key.substring(0,key.lastIndexOf('=')+1);
				keys.add(key);
			}
			bis.close();
			return keys.toArray(new String[keys.size()]);
		} catch (Throwable e) {
			log.warn("计算SHA1 出错!!",e);
			return null;
		}
	}
	
	protected static byte[] SHA1(MessageDigest sha1, InputStream in, int len) {
		sha1.reset();
		byte[] data = new byte[8196];
		int i = 0;
		int rsize = 0;
		while(len > 0) {
			if(len > data.length)
				rsize = data.length;
			else
				rsize = len;
			try {
				i = in.read(data,0,rsize);
			} catch (IOException e) {
				throw Lang.wrapThrow(e);
			}
			if(i == -1)
				break;
			sha1.update(data,0,i);
			len -= i;
		}
		if(len != 0)
			throw Lang.makeThrow("实际读取的数据长度与预期不一致!!");
		return sha1.digest();
	}
}
