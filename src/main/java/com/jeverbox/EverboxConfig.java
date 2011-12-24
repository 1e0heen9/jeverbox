package com.jeverbox;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.bean.EverBoxObject;
import com.snda.everbox.config.Config;
import com.snda.everbox.sdk.url.EverBoxServiceURLs;

public class EverboxConfig {

	public static final int UNKOWN = -1;
	public static final int FILE = 1;
	public static final int DIR = 2;
	
	public static final int DB_API_VERSION = 1;
	
	private static Properties p = new Properties();
	private static long configFileLastModify = 0;
	
	private static final Log log = Logs.get();
	
	public static final void init() {
		EverBoxServiceURLs.ACCOUNT_SERVER = "http://account.everbox.com";
		EverBoxServiceURLs.META_SERVER = "http://fs.everbox.com";
		EverBoxServiceURLs.IO_SERVER = "http://io.everbox.com";
		EverBoxServiceURLs.LOG_SERVER = "http://log.everbox.com";
		EverBoxServiceURLs.BIZ_SERVER = "http://biz.everbox.com";
		EverBoxServiceURLs.WWW_SERVER = "http://www.everbox.com";
		EverBoxServiceURLs.DL_SERVER = "http://dl.everbox.com";
		EverBoxServiceURLs.ACCOUNT_SERVER_SSL = "https://account.everbox.com";
		EverBoxServiceURLs.META_SERVER_SSL = "https://fs.everbox.com";
		EverBoxServiceURLs.IO_SERVER_SSL = "https://io.everbox.com";
		EverBoxServiceURLs.LOG_SERVER_SSL = "https://log.everbox.com";
		EverBoxServiceURLs.BIZ_SERVER_SSL = "https://biz.everbox.com";
		EverBoxServiceURLs.WWW_SERVER_SSL = "https://www.everbox.com";
		EverBoxServiceURLs.DL_SERVER_SSL = "https://dl.everbox.com";
		Config.userAgent = String.format("everbox4j(ver: %s, OS: %s)",VERSION,System.getProperty("os.name"));
		try {
			reloadConfig();
		} catch (Throwable e) {
		}
	}
	
	public static void reloadConfig() throws Throwable {
		File configFile = new File("./config.properties");
		if(configFile.exists() && configFile.lastModified() > configFileLastModify) {
			p.clear();
			p.load(new FileInputStream(configFile));
			configFileLastModify = configFile.lastModified();
		}
	}

	public static String getUserName(){
		return p.getProperty("username");
	}
	
	public static String getPassword(){
		return p.getProperty("password");
	}
	
	public static String getRootPath(){
		String rootPath = p.getProperty("rootpath","./everbox");
		if(rootPath.endsWith("/"))
			rootPath = rootPath.substring(0,rootPath.length() - 1);
		return rootPath;
	}
	
	public static String getRealPath(EverBoxObject ebo){
		String rPath;
		String path = ebo.getPath();
		if(path.equals("/home"))
			rPath = getRootPath() + "/";
		else
			rPath = getRootPath() +"/"+ ebo.getPath().substring(6);
		rPath = Disks.getCanonicalPath(rPath);
		try {
			rPath = new File(rPath).getCanonicalPath();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println(rPath);
		return rPath;
	}
	
	public static boolean isUploadEnable(){
		return "true".equalsIgnoreCase(p.getProperty("upload.enable"));
	}
	
	public static boolean isDownloadEnable(){
		return "true".equalsIgnoreCase(p.getProperty("download.enable"));
	}
	
	public static boolean matchUpload(EverBoxObject ebo){
		String path = EverboxConfig.getRealPath(ebo);
		if("/home".equals(ebo.getPath()) || ebo.getPath().startsWith("/home/.everbox"))
			return false;
		File file = new File(path);
		if(!file.exists()) {
			log.infof("不可能的啊,需要上传的文件不存在? %s",file.getAbsoluteFile());
			return false;
		}
		String myPath = ebo.getPath().substring(5);
		String ignorePath = p.getProperty("upload.ignore.path");
		if(!Strings.isBlank(ignorePath)) {
			String[] ps = ignorePath.split(",");
			for (String p : ps) {
				if(myPath.startsWith(p))
					return false;
			}
		}
		if(file.isDirectory())
			return true;
		String ignoreHidden = p.getProperty("upload.ignore.hidden");
		if(!Strings.isBlank(ignoreHidden)) {
			if("true".equalsIgnoreCase(ignoreHidden) && file.isHidden())
				return false;
		}
		String ignoreSuffix = p.getProperty("upload.ignore.suffix");
		if(!Strings.isBlank(ignoreSuffix)) {
			String[] iss = ignoreSuffix.split(",");
			String suffix = Files.getSuffixName(myPath);
			for (String is : iss) {
				if(suffix.equalsIgnoreCase(is))
					return false;
			}
		}
		String ignoreMaxsize = p.getProperty("upload.ignore.maxsize");
		if(!Strings.isBlank(ignoreMaxsize)) {
			return file.length() <= string2size(ignoreMaxsize);
		}
		String ignoreMinsize = p.getProperty("upload.ignore.minsize");
		if(!Strings.isBlank(ignoreMinsize)) {
			return file.length() >= string2size(ignoreMinsize);
		}
		return true;
	}
	
	public static boolean matchDownload(EverBoxObject ebo){
		String path = ebo.getPath();
		String ignorePath = p.getProperty("download.ignore.path");
		if(!Strings.isBlank(ignorePath)) {
			String[] ps = ignorePath.split(",");
			for (String p : ps) {
				if(ebo.getPath().startsWith(p))
					return false;
			}
		}
		if(ebo.getType() == EverboxConfig.DIR)
			return true;
		String ignoreSuffix = p.getProperty("download.ignore.suffix");
		if(!Strings.isBlank(ignoreSuffix)) {
			String[] iss = ignoreSuffix.split(",");
			String suffix = Files.getSuffixName(path);
			for (String is : iss) {
				if(suffix.equalsIgnoreCase(is))
					return false;
			}
		}
		String ignoreMaxsize = p.getProperty("download.ignore.maxsize");
		if(!Strings.isBlank(ignoreMaxsize)) {
			return ebo.getFileSize() <= string2size(ignoreMaxsize);
		}
		String ignoreMinsize = p.getProperty("download.ignore.minsize");
		if(!Strings.isBlank(ignoreMinsize)) {
			return ebo.getFileSize() >= string2size(ignoreMinsize);
		}
		return true;
	}
	
	public static EverBoxObject loadRemoteHomeDataFromDisk(){
		return readEverBoxObjectFromFile("remote.home.data");
	}
	
	public static EverBoxObject loadLocalHomeDataFromDisk(){
		return readEverBoxObjectFromFile("local.home.data");
	}
	
	private static EverBoxObject readEverBoxObjectFromFile(String fileName){
		try {
			File file = new File(fileName);
			if(file.exists()) {
				return Json.fromJson(EverBoxObject.class, Streams.fileInr(file));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EverBoxObject ebo = new EverBoxObject();
		ebo.setPath("/home");
		ebo.setEditTime(0);
		ebo.setType(EverboxConfig.DIR);
		return ebo;
	}
	
	public static boolean writeRemoteHomeData2Disk(EverBoxObject ebo){
		return writeEverBoxObject2File(ebo,"remote.home.data");
	}
	
	public static boolean writeLocalHomeData2Disk(EverBoxObject ebo){
		return writeEverBoxObject2File(ebo, "local.home.data");
	}
	
	private static boolean writeEverBoxObject2File(EverBoxObject ebo, String fileName){
		try {
			new File(fileName).createNewFile();
			Json.toJson(Streams.fileOutw(fileName), ebo);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static long string2size(String str){
		if(Strings.isBlank(str))
			return 0;
		str = str.trim().toLowerCase();
		if(str.endsWith("k"))
			return (long)(1024 * Double.parseDouble(str.substring(0,str.length() - 1)));
		if(str.endsWith("m"))
			return (long)(1024 * 1024 * Double.parseDouble(str.substring(0,str.length() - 1)));
		if(str.endsWith("g"))
			return (long)(1024 * 1024 * 1024 * Double.parseDouble(str.substring(0,str.length() - 1)));
		return (long)(Double.parseDouble(str));
	}
	
	public static String get(String key){
		return p.getProperty(key, "");
	}
	
	/**
	 * 仅限开发时使用
	 * @return
	 */
	public static Properties getP(){
		return p;
	}

	public static final String VERSION = "1.1";
}
