package com.jeverbox.bean;

import java.util.List;

public class EverboxUploadInfo {
	
	private List<EverboxUploadInfo> required;
	private int index;
	private String url;
	
	public List<EverboxUploadInfo> getRequired() {
		return required;
	}
	public void setRequired(List<EverboxUploadInfo> required) {
		this.required = required;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
