package com.jeverbox.bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import org.nutz.dao.entity.annotation.Name;

@Table("t_download_info")
public class EverboxDownloadInfo {

	@Id
	@Column
	private int id;
	
	@Name
	@Column
	private String path;
	
	@Column
	private String tempFilePath;
	
	@Column
	private String ver;
	
//	private long size;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTempFilePath() {
		return tempFilePath;
	}
	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
//	public long getSize() {
//		return size;
//	}
//	public void setSize(long size) {
//		this.size = size;
//	}
	
}
