package com.jeverbox.bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

@Table("t_local_file_info")
public class EverboxLocalFileInfo {

	@Id
	private long id;
	
	@Column
	@Name
	private String path;
	
	@Column
	private int type;
	
	@Column
	private String sha1s;
	
	@Column
	private String remoteRev;
	
	@Column("lm")
	private long lastModify;
	
	public long getLastModify() {
		return lastModify;
	}
	public void setLastModify(long lastModify) {
		this.lastModify = lastModify;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getSha1s() {
		return sha1s;
	}
	public void setSha1s(String sha1s) {
		this.sha1s = sha1s;
	}
	public String getRemoteRev() {
		return remoteRev;
	}
	public void setRemoteRev(String remoteRev) {
		this.remoteRev = remoteRev;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
