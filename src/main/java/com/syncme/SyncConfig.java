package com.syncme;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.EverboxConfig;
import com.jeverbox.bean.EverBoxObject;

public class SyncConfig {


	public static final String VERSION = "1.1";
	
	private static File configFile;
	
	private static Properties p = new Properties();
	private static long configFileLastModify = 0;
	
	private static final Log log = Logs.get();
	
	public static void setConfigFile(String configFile) {
		SyncConfig.configFile = new File(configFile);
	}
	
	public static void reloadConfig() throws Throwable {
		if(configFile.exists() && configFile.lastModified() > configFileLastModify) {
			p.clear();
			p.load(new FileInputStream(configFile));
			configFileLastModify = configFile.lastModified();
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
	
	public static String getUserName(){
		return p.getProperty("username");
	}
	
	public static String getPassword(){
		return p.getProperty("password");
	}
	
	public static String getRootPath(){
		return p.getProperty("rootpath");
	}
	
	public static String getRealPath(EverBoxObject ebo){
		return getRootPath() +"/"+ ebo.getPath().substring(5);
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

}
