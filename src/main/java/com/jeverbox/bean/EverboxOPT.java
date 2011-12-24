package com.jeverbox.bean;

/**
 *
 * @author wendal
 */
public class EverboxOPT {
	public static final int ADD = 1;/*新增,添加 -- 对应服务器端为上传,对本地来说就是下载*/
	public static final int DELETE = 2;/*删除*/

	private EverBoxObject everBoxObject;
	private int mode;
	
	public EverBoxObject getEverBoxObject() {
		return everBoxObject;
	}
	public void setEverBoxObject(EverBoxObject everBoxObject) {
		this.everBoxObject = everBoxObject;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	
}
