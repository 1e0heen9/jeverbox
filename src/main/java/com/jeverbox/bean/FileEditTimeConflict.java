package com.jeverbox.bean;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

@Table("t_file_lm_conflict")
public class FileEditTimeConflict {
	
	@Id
	private long id;
	
	@Name
	private String path;
	
	@Column("lm")
	private long lastModify;
	
	@Column("api")
	private int apiVerion;

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

	public long getLastModify() {
		return lastModify;
	}

	public void setLastModify(long lastModify) {
		this.lastModify = lastModify;
	}

	public int getApiVerion() {
		return apiVerion;
	}
	
	public void setApiVerion(int apiVerion) {
		this.apiVerion = apiVerion;
	}
	
	
}
