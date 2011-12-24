package com.jeverbox;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.bean.EverBoxObject;
import com.jeverbox.bean.EverboxDownloadInfo;
import com.jeverbox.bean.EverboxLocalFileInfo;
import com.jeverbox.bean.EverboxUploadInfo;
import com.snda.everbox.config.Config;
import com.snda.everbox.consts.Constants;
import com.snda.everbox.fs.UploadProgressListener;
import com.snda.everbox.sdk.account.Account;
import com.snda.everbox.sdk.auth.Auth;
import com.snda.everbox.sdk.auth.InnerToken;
import com.snda.everbox.sdk.fs.FS;
import com.snda.everbox.sdk.proxy.EverBoxJsonProxy;
import com.snda.everbox.sdk.url.EverBoxServiceURLs;

public final class EverboxAPI {

	private static final Log log = Logs.get();
	
	public static EverBoxObject get(String path){
		log.info("尝试获取文件信息 "+ path);
		try {
			JSONObject obj = new JSONObject();
			obj.put("path", path);
			StringBuilder sb = new StringBuilder();
			int rc = FS.get(obj, sb);
			if(rc != 200)
				return null;
			log.info("成功获取文件信息 "+ path);
			return Json.fromJson(EverBoxObject.class, sb);
		} catch (Throwable e) {
			log.warn("Fail to get path",e);
			return null;
		}
	}
	
	public static void download(EverBoxObject ebo){
		try {
			log.info("准备下载文件 "+ ebo.getPath());
			//判断是否为文件夹
			if(ebo.getType() == EverboxConfig.DIR) {
				log.info("是一个文件夹,直接在本地创建 "+ ebo.getPath());
				Files.createDirIfNoExists(EverboxConfig.getRealPath(ebo));
				loadLocalFileInfo(ebo);
				EverboxContext.registerFileConflict(ebo);
				return;
			}
			if(ebo.getFileSize() == 0) {
				log.info("需要下载的是一个空文件,直接创建,无需向服务器请求数据 "+ ebo.getPath());
				Files.createFileIfNoExists(EverboxConfig.getRealPath(ebo));
				return;
			}
			
			JSONObject obj = new JSONObject();
			obj.put("path", ebo.getPath());
//			obj.put("aimType", 0);
			obj.put("showType", 3);
			obj.put("base", "");
			obj.put("list", 1);
			StringBuilder sb = new StringBuilder();
			int rc = FS.get(obj, sb);
			if(rc != 200) {
				log.warn("下载失败!! 查询下载地址时,服务器响应异常");
				//TODO 加入重试队列
				return;
			}
			JSONObject thumbnailInfo = new JSONObject(sb.toString());
			if(!thumbnailInfo.has("dataurl")) {
				log.warn("下载失败!! 查询下载地址时,服务器响应没有包含下载地址!!");
				//TODO 加入重试队列
				return;
			}
			log.debug("Server return : " + thumbnailInfo);
			log.debug("获取下载URL "+ thumbnailInfo.getString("dataurl"));
			
			//额外检查,以免重复下载了文件
			EverBoxObject localEbo = new EverBoxObject();
			localEbo.setPath(ebo.getPath());
			loadLocalFileInfo(localEbo);
			if(localEbo.getType() == 1 && localEbo.getFileSize() == ebo.getFileSize()) {
				log.info("虽然服务器上的文件的最后修改时间比本地文件新,但大小与一致,需要额外检查 "+ ebo.getPath());
				if(thumbnailInfo.has("fileSize")) {
					long fileSize = thumbnailInfo.getLong("fileSize");
					if(fileSize == localEbo.getFileSize()) {
						String[] keys = makeFileKeys(ebo);
						EverboxLocalFileInfo info = EverboxContext.dao.fetch(EverboxLocalFileInfo.class, ebo.getPath());
						if(info != null && info.getRemoteRev() != null && info.getSha1s() != null) {
							if(info.getRemoteRev().equals(thumbnailInfo.getString("ver")) && info.getSha1s().equals(Json.toJson(keys))) {
								log.info("本地文件与服务器文件一致,无需下载");
								return;
							}
						}
						File f = new File(EverboxConfig.getRealPath(ebo));
						if(!preparePut(ebo, keys)) {
							log.info("本地文件与服务器文件一致,无需下载");
							//TODO XXX 登记到数据库
							saveLocalFileInfo2DB(f, ebo, keys, thumbnailInfo.getString("ver"));
							return;
						}
					}
				}//TODO 检测一下重命名的情况
			}

		    //使用临时文件,以实现断点续传
		    EverboxDownloadInfo dInfo = EverboxContext.getDownloadInfo(ebo.getPath());
		    File tmpFile = null;
		    boolean append = false;
		    if(dInfo != null && ebo.getVer().equals(dInfo.getVer())) {
		    	log.debug("查询到之前的下载信息,该文件之前下载过,尝试找回原本的临时文件");
		    	tmpFile = new File(dInfo.getTempFilePath());
		    	if(tmpFile.exists()){
		    		log.debugf("已经下载过%d个字节",tmpFile.length());
		    		append = true;
		    	}
		    } 
		    if(tmpFile == null || (!tmpFile.exists())){
		    	log.debug("创建临时文件");
		    	tmpFile = File.createTempFile("everbox4j_download", null);
		    	dInfo = new EverboxDownloadInfo();
		    	dInfo.setPath(ebo.getPath());
		    	dInfo.setTempFilePath(tmpFile.getAbsolutePath());
		    	dInfo.setVer(ebo.getVer());
		    }
		    EverboxContext.saveDownloadInfo(dInfo);
			HttpGet httpGet = new HttpGet(thumbnailInfo.getString("dataurl"));
		    String str = Config.getUserAgent();
		    httpGet.setHeader("User-Agent", str);
		    if(tmpFile.length() > 0) //断点续传
		    	httpGet.setHeader("Range", "bytes="+tmpFile.length()+"-");
		    DefaultHttpClient httpClient = new DefaultHttpClient();
		    httpClient.getParams().setParameter("http.connection.timeout", 10000);
		    HttpResponse resp = httpClient.execute(httpGet);
		    if (resp.getStatusLine().getStatusCode() >= 300) {
		    	log.warn("下载失败!! 访问下载地址,服务器的响应不正确!!!!");
				//TODO 加入重试队列
				return;
		    }
		    log.debug("访问下载URL成功,开始读取服务器的响应");
			FileOutputStream fos = new FileOutputStream(tmpFile,append);
			Streams.write(fos, resp.getEntity().getContent());
			File destFile = Files.createFileIfNoExists(EverboxConfig.getRealPath(ebo));
			destFile.delete();
			if(!tmpFile.renameTo(destFile)) {//直接改名失败,只会使用复制了
				FileInputStream in = new FileInputStream(tmpFile);
				FileOutputStream out = new FileOutputStream(destFile);
				Streams.write(out, in);
			}
			log.debug("下载成功");
			loadLocalFileInfo(ebo);
			EverboxContext.registerFileConflict(ebo);
			try{
				tmpFile.delete();
				EverboxContext.removeDownloadInfo(dInfo);
			}catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			log.warn("Fail to download file " + ebo.getPath(),e);
		}
	}
	
	public static void upload(EverBoxObject ebo) {
		try {
			log.info("准备上传文件 "+ ebo.getPath());
			//---------------------------------------------------------------------
			String[] keys = makeFileKeys(ebo);
			if(keys == null) {
				log.info("上传文件失败!! "+ ebo.getPath());
				return;
			}
			if(checkLocalFileModify(ebo, keys)) {
				log.info("本地文件未曾修改,无需上传!! "+ ebo.getPath());
				EverboxContext.registerFileConflict(ebo);
				return;
			}
			File f = new File(EverboxConfig.getRealPath(ebo));
			JSONObject obj = new JSONObject();
			//prepare_put
			obj.put("base", "");
			obj.put("path", ebo.getPath());
			obj.put("chunkSize", Constants.FILE_CHUNK_SIZE);
			obj.put("fileSize", f.length());
			int part = keys.length;
			log.infof("将文件分成%d段,进行上传前分析",part);
			obj.put("keys", keys);
			StringBuilder sb = new StringBuilder();
			int rc = FS.preparePut(obj, sb);
			if(rc == 409) {//冲突了!看来这个文件已经于服务器的文件一致,OK,不再上传
				log.info("冲突了!看来这个文件已经与服务器的文件一致,OK,不再上传 " + ebo.getPath());
				EverboxContext.registerFileConflict(ebo);
				return;
			}
			if(rc != 200) {
				log.info("上传出错啦!! "+ sb.toString() + "" + ebo.getPath());
				return;
			}
			//---------------------------------------------------------------------------
			EverboxUploadInfo uploadInfo = Json.fromJson(EverboxUploadInfo.class, sb);
			if(uploadInfo.getRequired() != null && uploadInfo.getRequired().size() > 0) {
				log.infof("需要上传%d段",uploadInfo.getRequired().size());
				RandomAccessFile raf = new RandomAccessFile(f, "r");
				List<EverboxUploadInfo> infos = uploadInfo.getRequired();
				long lastPartsize = f.length() - (part-1)*Constants.FILE_CHUNK_SIZE;
				for (int i = 0; i < infos.size(); i++) {
					EverboxUploadInfo info = infos.get(i);
					log.infof("开始上传第%d段",info.getIndex());
					raf.seek(info.getIndex() * Constants.FILE_CHUNK_SIZE);
					sb = new StringBuilder();
					EverBoxJsonProxy ebjp = new EverBoxJsonProxy();
					if(info.getIndex() + 1 < part) {
						rc = ebjp.CallPostFileStream(info.getUrl(), raf, Constants.FILE_CHUNK_SIZE, sb, new UploadProgressListener());
					} else {
						rc = ebjp.CallPostFileStream(info.getUrl(), raf, (int)lastPartsize, sb, new UploadProgressListener());
					}

					if(rc != 200) {
						log.info("上传出错啦!! "+ sb.toString() + " " + ebo.getPath());
						return;
					}
				}
			} else {
				log.info("无需上传任何数据");
			}
			sb = new StringBuilder();
			
			
			//commit_put
			obj.put("editTime", f.lastModified());
			obj.put("mimeType", "unknown");
			if(200 != FS.commitPut(obj, sb)) {
				log.info("上传失败!! 最后一步提交失败!!");
				return;
			}
			//登记到数据库
			saveLocalFileInfo2DB(f, ebo, keys, new JSONObject(sb.toString()).getString("ver"));
		} catch (Throwable e) {
			log.warn("Fail to upload file",e);
		}
	}
	
	protected static void saveLocalFileInfo2DB(File f,EverBoxObject ebo, String[] keys, String ver){
		//登记到数据库
		EverboxLocalFileInfo info = new EverboxLocalFileInfo();
		info.setLastModify(f.lastModified());
		info.setPath(ebo.getPath());
		info.setSha1s(Json.toJson(keys));
		info.setRemoteRev(ver);
		info.setType(EverboxConfig.FILE);
		EverboxContext.saveOrUpdate(info, "path");
	}
	
	public static boolean preparePut(EverBoxObject ebo, String[] keys) throws Throwable {
		if(keys == null)
			keys = makeFileKeys(ebo);
		if(keys == null)
			return false;
		//---------------------------------------------------------------------
		File f = new File(EverboxConfig.getRealPath(ebo));
		JSONObject obj = new JSONObject();
		//prepare_put
		obj.put("base", "");
		obj.put("path", ebo.getPath());
		obj.put("chunkSize", Constants.FILE_CHUNK_SIZE);
		obj.put("fileSize", f.length());
		
		obj.put("keys", keys);
		StringBuilder sb = new StringBuilder();
		int rc = FS.preparePut(obj, sb);
		if(rc == 409) {//冲突了!看来这个文件已经于服务器的文件一致
			log.info("冲突了!看来这个文件已经与服务器的文件一致,OK " + ebo.getPath());
			EverboxContext.registerFileConflict(ebo);
			return false;
		}
		return rc == 200;
		//---------------------------------------------------------------------------
	}
	
	public static int mkidrRemote(EverBoxObject ebo) throws Throwable{
		System.out.println("创建远程文件夹 "+ebo.getPath());
		JSONObject jo = new JSONObject();
		jo.put("path", ebo.getPath());
		jo.put("editTime", ebo.getEditTime());
		EverBoxJsonProxy ebjp = new EverBoxJsonProxy();
		StringBuilder sb = new StringBuilder();
		int rc = ebjp.CallPost(EverBoxServiceURLs.META_SERVER + "/mkdir", jo, "UTF-8", sb);
		if(rc != 200)
			EverboxContext.registerFileConflict(ebo);
		return rc;
	}
	
	public static void deleteRemote(EverBoxObject ebo) throws Throwable {
		JSONObject jo = new JSONObject();
		jo.put("paths", new Object[]{ebo.getPath()});
		EverBoxJsonProxy ebjp = new EverBoxJsonProxy();
		StringBuilder sb = new StringBuilder();
		ebjp.CallPost("/delete", jo, "UTF-8", sb);
	}
	
	/**
	 * 计算需要创建的远程文件夹
	 */
	public static List<String> countDirNeedCreate(List<EverBoxObject> src, List<EverBoxObject> news){
		List<String> srcList = new ArrayList<String>();
		for (EverBoxObject ebo : src) {
			if(ebo.getType() == EverboxConfig.DIR)
				srcList.add(ebo.getPath());
		}
		List<String> list = new ArrayList<String>();
		for (EverBoxObject ebo : news) {
			String fPath = ebo.getPath();
			String dir = fPath.substring(0,fPath.lastIndexOf("/"));
			if(src.contains(dir))
				continue;
			String[] ds = dir.substring(1).split("/");
			String currentDir = "/home";
			for (int i = 1; i < ds.length; i++) {
				currentDir += "/" + ds[i];
				if(srcList.contains(currentDir))
					continue;
				if(list.contains(currentDir))
					continue;
				list.add(currentDir);
			}
		}
		Collections.sort(list);
		return list;
	}

	public static void login(){
		try {
			log.info("开始登录");
			EverboxConfig.init();
			if(ping())
				return;
			File file = new File("everbox.data");
			if(file.exists()) {
				log.info("找到以前的登录验证数据,尝试快速登录");
				BufferedReader br = new BufferedReader(new FileReader(file));
				InnerToken it = new InnerToken(br.readLine(), br.readLine());
				Auth.setAccessToken(it);
				if(ping()) {
					log.info("登录成功,可以使用之前的登录验证数据");
					return;
				}
			}
			log.info("开始常规登录");
			int rc = Account.login(EverboxConfig.getUserName(), EverboxConfig.getPassword());
			if(rc != 200) {
				log.info("常规登录失败!!! 请检查用户名和密码!!");
				throw new RuntimeException("登录失败");
			}
			InnerToken it = Auth.getAccessToken();
			FileWriter fw = new FileWriter(file);
			fw.write(it.getToken());
			fw.write("\n");
			fw.write(it.getSecret());
			fw.flush();
			fw.close();
			log.info("记录本次登录验证信息,用于以后的快速登录");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean ping(){
		if(Auth.getAccessToken() != null) {
			try {
				int rc = Account.ping();
				return rc == 200;
			} catch (Throwable e) {
				log.warn("Ping fail!!",e);
			}
		}
		return false;
	}
	
	public static EverBoxObject cloneEverboxObject(EverBoxObject ref){
		return Json.fromJson(EverBoxObject.class, Json.toJson(ref));
	}
	
	public static void compareEverboxObject(EverBoxObject oldOne, EverBoxObject newOne,List<EverboxModify> mList){
		List<EverBoxObject> oldList = new ArrayList<EverBoxObject>(oldOne.getEntries());
		List<EverBoxObject> newList = new ArrayList<EverBoxObject>(newOne.getEntries());
		oldIT: for (Iterator<EverBoxObject> oldIt = oldList.iterator(); oldIt.hasNext();) {
			EverBoxObject oldEntry = oldIt.next();
			for (Iterator<EverBoxObject> newIt = newList.iterator(); newIt.hasNext();) {
				EverBoxObject newEntry = newIt.next();
				if(newEntry.getPath().equals(oldEntry.getPath())) {
					newIt.remove();
					if(newEntry.getType() != oldEntry.getType()) {
						log.infof("警告!! 文件类型(文件夹<-->文件)改变!! -- (%d-->%d) %s",
								oldEntry.getType() , newEntry.getType(), oldEntry.getPath());
					}
					if(newEntry.getType() == EverboxConfig.FILE && oldEntry.getType() == EverboxConfig.FILE) {
						if(oldEntry.getEditTime() > newEntry.getEditTime()) {
							mList.add(new EverboxModify(oldEntry, EverboxModify.OLDER, false));
							log.info("B的文件较旧-- " + oldEntry.getPath());
						} else if(oldEntry.getEditTime() < newEntry.getEditTime()) {
							mList.add(new EverboxModify(newEntry, EverboxModify.NEWER, false));
							log.info("B的文件较新 -- " + oldEntry.getPath());
						}
					}else if(newEntry.getType() == EverboxConfig.DIR && oldEntry.getType() == EverboxConfig.DIR) {
						compareEverboxObject(oldEntry, newEntry,mList);
					}
					continue oldIT;
				}
		  }
		  //能到这里? 那肯定是被删除了
		  oldIt.remove();
		  List<EverBoxObject> list = new ArrayList<EverBoxObject>();
		  oldEntry.asList(list);
		  for (EverBoxObject ebo : list) {
			  log.info("B比A少了一个文件 -- " + ebo.getPath());
			  mList.add(new EverboxModify(ebo, EverboxModify.MISS, false));
		  }
		}
		//仍然有新的Entry? 那就是新增咯
		if(newList.size() > 0) {
			for (EverBoxObject newEntry : newList) {
				List<EverBoxObject> list = new ArrayList<EverBoxObject>();
				newEntry.asList(list);
				for (EverBoxObject ebo : list) {
					mList.add(new EverboxModify(ebo, EverboxModify.ADD, false));
					log.info("B比A多了一个文件 -- " + ebo.getPath());
				}
			}
		}
	}
	
	public static EverBoxObject listLocal(EverBoxObject parent) throws Throwable {
		if(parent.getType() == EverboxConfig.UNKOWN)
			loadLocalFileInfo(parent);
		if(parent.getType() == EverboxConfig.FILE) {
			return parent;
		}
		File parentFile = new File(EverboxConfig.getRealPath(parent));
//		System.out.println(parentFile.getAbsolutePath());
		File[] files = parentFile.listFiles();
		if(files == null)
			files = new File[0];
		List<EverBoxObject> entries = new ArrayList<EverBoxObject>(files.length);
		for (File file : files) {
			EverBoxObject ebo = new EverBoxObject();
			ebo.setPath(parent.getPath() +"/" + file.getName());
			loadLocalFileInfo(ebo);
			entries.add(ebo);
			if(parent.getType() == EverboxConfig.DIR)
				listLocal(ebo);
		}
		parent.setEntries(entries);
		return parent;
	}
	
	public static void loadLocalFileInfo(EverBoxObject ebo) {
		File file = new File(EverboxConfig.getRealPath(ebo));
		if(!file.exists())
			return;
		if(file.isDirectory()) {
			ebo.setType(EverboxConfig.DIR);
		} else if(file.isFile()){
			ebo.setType(EverboxConfig.FILE);
			ebo.setFileSize(file.length());
		}
		ebo.setEditTime(file.lastModified() * 10000);
	}
	
	public static boolean checkLocalFileModify(EverBoxObject ebo,String[] keys){
		EverboxLocalFileInfo x = EverboxContext.get(EverboxLocalFileInfo.class, "path", ebo.getPath());
		if(x != null && x.getSha1s() != null) {
			if(Json.toJson(keys).equals(x.getSha1s()))
				return true;//Not modify
		}
		return false;//Changed
	}
	
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
//			System.out.println("SHA1 read : " + i);
			if(i == -1)
				break;
			sha1.update(data,0,i);
			len -= i;
		}
//		System.out.println(len);
		if(len != 0)
			throw Lang.makeThrow("实际读取的数据长度与预期不一致!!");
		return sha1.digest();
	}
}
