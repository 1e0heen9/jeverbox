package com.syncme.bean;

import java.util.ArrayList;
import java.util.List;

import com.jeverbox.EverboxConfig;

public class SyncObject {

	private long fileSize;
	private long editTime;
	private String path;
//	private String ver;
	private int type = EverboxConfig.UNKOWN;
//	private long fileCount;
	
	private List<SyncObject> entries;
	
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public long getEditTime() {
		return editTime;
	}
	public void setEditTime(long editTime) {
		this.editTime = editTime;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<SyncObject> getEntries() {
		if(entries == null)
			entries = new ArrayList<SyncObject>();
		return entries;
	}
	public void setEntries(List<SyncObject> entries) {
		this.entries = entries;
	}
//	public String getVer() {
//		return ver;
//	}
//	public void setVer(String ver) {
//		this.ver = ver;
//	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
//	public long getFileCount() {
//		return fileCount;
//	}
//	public void setFileCount(long fileCount) {
//		this.fileCount = fileCount;
//	}
	
	public void asList(List<SyncObject> list){
		list.add(this);
		for (SyncObject syncObject : getEntries()) {
			syncObject.asList(list);
		}
	}
}
