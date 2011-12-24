package com.jeverbox;

import com.jeverbox.bean.EverBoxObject;

public class EverboxModify {

	public static final int ADD = 0;
	public static final int MISS = 1;
	public static final int NEWER = 2;
	public static final int OLDER = 3;
//	public static final int UPDATE = 4;
	public static final int CONFLICT = 8;

	/**
	 * 这个修改所对应的EverBoxObject
	 */
	private EverBoxObject everBoxObject;
	
	/**
	 * 变更类型
	 */
	private int mode;
	
	/**
	 * 该变更是否为服务器端的修改
	 */
	private boolean remote;

	public EverboxModify(EverBoxObject everBoxObject, int mode, boolean remote) {
		super();
		this.everBoxObject = everBoxObject;
		this.mode = mode;
		this.remote = remote;
	}

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

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}
	
	
}
