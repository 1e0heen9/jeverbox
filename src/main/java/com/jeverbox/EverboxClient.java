package com.jeverbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.bean.EverBoxObject;
import com.jeverbox.bean.EverboxOPT;
import com.jeverbox.bean.EverboxRunningContext;
import com.snda.everbox.sdk.fs.FS;

/**
 *
 * @author wendal
 */
@IocBean
public class EverboxClient implements Runnable {
	
	public boolean run;
	
	private EverboxRunningContext context = new EverboxRunningContext();

	//保存远程(服务器端)的变更
	private List<EverboxModify> remoteModifyList = new ArrayList<EverboxModify>();
	
	private static final Log log = Logs.get();
	
	public EverboxClient() throws Throwable{
		log.infof("Everbox4j Client %s , see http://everbox4j.googlecode.com",EverboxConfig.VERSION);
		log.infof("我的博客 http://wendal.net");
	}
	
	public void run() {
		try {
			runme();
		} catch (Throwable e) {
			log.error("客户端意外终止!!!",e);
		}
	}
	
	public void runme() throws Throwable {
		run = true;
		EverboxAPI.login();
		while(run) {
			//重新加载一下配置
			EverboxConfig.reloadConfig();
			//加载前一次运行后,本地文件情况
			context.preLocalHome = EverboxConfig.loadLocalHomeDataFromDisk();
			//刷新当前的本地文件情况
			EverBoxObject eboA = new EverBoxObject();
			eboA.setPath("/home");
			context.nowLocalHome = listLocal(eboA);
			//得出本地Home变化差异表A
			log.info("得出本地Home变化差异表");
			context.localModifyList.clear();
			EverboxAPI.compareEverboxObject(EverboxAPI.cloneEverboxObject(context.preLocalHome),
											EverboxAPI.cloneEverboxObject(context.nowLocalHome), context.localModifyList);
			//加载前一次运行后,远程文件情况
			context.preRemoteHome = EverboxConfig.loadRemoteHomeDataFromDisk();
			//刷新当前的远程文件情况,得出差异表B
			log.info("刷新当前的远程文件情况,得出差异表");
			context.nowRemoteHome = reflashRemote(EverboxAPI.cloneEverboxObject(context.preRemoteHome), 
					EverboxAPI.get("/home"));
			context.remoteModifyList = remoteModifyList;
			//对比当前本地文件情况与当前远程文件情况,得出差异表C
			log.info("对比当前本地文件情况与当前远程文件情况");
			context.localRemoteDiffList.clear();
			EverboxAPI.compareEverboxObject(EverboxAPI.cloneEverboxObject(context.nowLocalHome), 
					EverboxAPI.cloneEverboxObject(context.nowRemoteHome), context.localRemoteDiffList);
			//综合差异表ABC,得出任务列表
			makeTasks();
			//执行任务列表
			executeTasks();
			//保存当前本地列表和远程文件列表
			EverboxConfig.writeLocalHomeData2Disk(context.nowLocalHome);
			EverboxConfig.writeRemoteHomeData2Disk(context.nowRemoteHome);
			//完成一轮,休息30s
			log.info("ALL done ,sleep 30s---------------------------------------------------------");
			int time = 0;
			while(run && time < 30) {
				Thread.sleep(1000);
				time++;
			}
		}
		log.info("客户端已经停止!");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable{
		new EverboxClient().runme();
	}
	
	/**
	 * 更新指定节点及其子节点的信息
	 */
	public EverBoxObject reflashRemote(EverBoxObject oldOne, EverBoxObject newOne) throws Throwable {
		log.info("开始检查 "+oldOne.getPath());
		boolean equal = equals(oldOne, newOne);
		if(!equal)
			mergeEverBoxObject(oldOne, newOne);
		//文件已经改变,遍历其Entries
		List<EverBoxObject> oldEntries = oldOne.getEntries();
		List<EverBoxObject> newEntries = newOne.getEntries();
		oldIT : for (Iterator<EverBoxObject> oldIt = oldEntries.iterator(); oldIt.hasNext();) {
			EverBoxObject oldEntry = oldIt.next();
			for (Iterator<EverBoxObject> newIt = newEntries.iterator(); newIt.hasNext();) {
				EverBoxObject newEntry = newIt.next();
				if(oldEntry.getPath().equals(newEntry.getPath())) {
					newIt.remove();
//					System.out.println("找到一个已存在文件 " + oldEntry.getPath());
					boolean flag = equals(oldEntry, newEntry);
					if(flag) {
//						System.out.println("没有做任何修改 "+oldEntry.getPath());
						continue oldIT;
					}
					if(oldEntry.getType() == EverboxConfig.FILE && newEntry.getType() == EverboxConfig.FILE) {
						mergeEverBoxObject(oldEntry, newEntry);
						remoteModifyList.add(new EverboxModify(newEntry, EverboxModify.NEWER, true));
						log.info("远程(服务器端)更新了一个文件 -- " + oldEntry.getPath());
						continue oldIT;//TODO add into remote modify list
					} else if(oldEntry.getType() != newEntry.getType()) {
//						remoteModifyList.add(new EverboxModify(newEntry, EverboxModify.UPDATE, true));
//						mergeEverBoxObject(oldEntry, newEntry);
						log.info("远程(服务器端) !警告!! 文件类型(文件夹<-->文件)改变!! -- " + oldEntry.getPath());
//						continue oldIT;//TODO add into remote modify list
					}
					JSONObject obj = new JSONObject();
					obj.put("path", newEntry.getPath());
					StringBuilder sb = new StringBuilder();
					FS.get(obj, sb);
					newEntry = Json.fromJson(EverBoxObject.class, sb.toString());
					reflashRemote(oldEntry, newEntry);
					continue oldIT;
				}
			}
			//No match in newOne? Object shall be deleted!!
			oldIt.remove();//TODO add into remote modify list
			remoteModifyList.add(new EverboxModify(oldEntry, EverboxModify.MISS, true));
			log.info("远程(服务器端)删除了一个文件 -- " + oldEntry.getPath());
		}
		//仍然有新的Entry? 那就是远程新增咯
		if(newEntries.size() > 0) {
			for (EverBoxObject newEntry : newEntries) {
				System.out.println(newEntry.getPath());
				oldEntries.add(listRemote(newEntry));//TODO add into remote modify list
				List<EverBoxObject> list = new ArrayList<EverBoxObject>();
				newEntry.asList(list);
				for (EverBoxObject ebo : list) {
					remoteModifyList.add(new EverboxModify(ebo, EverboxModify.ADD, true));
					log.info("远程(服务器端)新增了一个文件 -- " + ebo.getPath());
				}
			}
		}
		log.info("结束检查 "+oldOne.getPath());
		return oldOne;
	}
	
	public boolean equals(EverBoxObject oldOne, EverBoxObject newOne) {
		boolean flag = newOne.getVer().equals(oldOne.getVer());
		return flag;
	}
	
	private void mergeEverBoxObject(EverBoxObject oldOne, EverBoxObject newOne){
		oldOne.setEditTime(newOne.getEditTime());
		oldOne.setVer(newOne.getVer());
		oldOne.setFileCount(newOne.getFileCount());
		oldOne.setFileSize(newOne.getFileSize());
	}
	
	/**
	 * 遍历全部子节点
	 */
	public EverBoxObject listRemote(EverBoxObject parent) throws Throwable {
		if (parent.getType() != EverboxConfig.DIR)
			return parent;
		log.info("遍历全部子节点 "+parent.getPath());
		parent = EverboxAPI.get(parent.getPath());
		for (EverBoxObject ebo : parent.getEntries()) {
			if(ebo.getType() == EverboxConfig.DIR) {
				EverBoxObject me = EverboxAPI.get(ebo.getPath());
				me = listRemote(me);
				ebo.setEntries(me.getEntries());
			}
		}
		return parent;
	}
	
	public EverBoxObject listLocal(EverBoxObject parent) throws Throwable {
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
	
	public void loadLocalFileInfo(EverBoxObject ebo) {
		File file = new File(EverboxConfig.getRealPath(ebo));
		if(!file.exists())
			return;//impossible?
		if(file.isDirectory()) {
			ebo.setType(EverboxConfig.DIR);
		} else if(file.isFile()){
			ebo.setType(EverboxConfig.FILE);
			ebo.setFileSize(file.length());
		}
		ebo.setEditTime(file.lastModified() * 10000);
	}

	/**
	 * TODO 要生成2份任务表:
	 * 1. 下载列表
	 * 2. 上传列表
	 * 3. 本地删除列表
	 * 4. 服务器端操作列表
	 */
	public void makeTasks(){
		context.remoteOpts.clear();
		context.localOpts.clear();
		if(EverboxConfig.isUploadEnable() && EverboxConfig.isDownloadEnable())
			log.info("当前版本不支持上传和下载同时启用,自动改为上传模式");
		if(EverboxConfig.isUploadEnable()){//仅上传服务器没有的文件,或本地已经更新的文件
			log.info("当前模式: 上传模式");
			for (EverboxModify ebm : context.localRemoteDiffList) {
				if(ebm.getMode() == EverboxModify.MISS || ebm.getMode() == EverboxModify.OLDER)
					if(EverboxConfig.matchUpload(ebm.getEverBoxObject())
							&& EverboxContext.checkFileConflict(ebm.getEverBoxObject())) {
						EverboxOPT opt = new EverboxOPT();
						opt.setMode(EverboxOPT.ADD);
						opt.setEverBoxObject(ebm.getEverBoxObject());
						context.remoteOpts.add(opt);
					}
			}
		}else if(EverboxConfig.isDownloadEnable()) {
			log.info("当前模式: 下载");
			for (EverboxModify ebm : context.localRemoteDiffList) {
				if(ebm.getMode() == EverboxModify.ADD || ebm.getMode() == EverboxModify.NEWER)
					if(EverboxConfig.matchDownload(ebm.getEverBoxObject())) {
						EverboxOPT opt = new EverboxOPT();
						opt.setMode(EverboxOPT.ADD);
						opt.setEverBoxObject(ebm.getEverBoxObject());
						context.localOpts.add(opt);
					} else
						log.info("文件不符合过滤条件,自动跳过 "+ ebm.getEverBoxObject().getPath());
			}
		}
	}
	
	/**
	 * TODO 执行4份任务表
	 */
	public void executeTasks() throws Throwable {
//		System.out.println("-------------------------------------------------------");
//		System.out.println(Json.toJson(context.remoteOpts));
//		System.out.println("-------------------------------------------------------");
//		System.out.println(Json.toJson(context.localOpts));
//		System.out.println("-------------------------------------------------------");
//		System.exit(1);
		for (Iterator<EverboxOPT> it = context.remoteOpts.iterator(); it.hasNext();) {
			EverboxOPT opt = it.next();
			EverBoxObject ebo = opt.getEverBoxObject();
			if(opt.getMode() == EverboxOPT.ADD) {
				if(ebo.getType() == EverboxConfig.FILE) {
					EverboxAPI.upload(ebo);
				} else if(ebo.getType() == EverboxConfig.DIR) {
					EverboxAPI.mkidrRemote(ebo);
				}
			} else if(opt.getMode() == EverboxOPT.DELETE) {
				EverboxAPI.deleteRemote(ebo);
			}
		}
		for (Iterator<EverboxOPT> it = context.localOpts.iterator(); it.hasNext();) {
			EverboxOPT opt = it.next();
			EverBoxObject ebo = opt.getEverBoxObject();
			if(opt.getMode() == EverboxOPT.ADD) {
				if(ebo.getType() == EverboxConfig.FILE) {
					EverboxAPI.download(ebo);
				} else if(ebo.getType() == EverboxConfig.DIR) {
					Files.makeDir(new File(EverboxConfig.getRealPath(ebo)));
				}
			} else if(opt.getMode() == EverboxOPT.DELETE) {
				Files.deleteDir(new File(EverboxConfig.getRealPath(ebo)));
			}
		}
	}
	
	
}
