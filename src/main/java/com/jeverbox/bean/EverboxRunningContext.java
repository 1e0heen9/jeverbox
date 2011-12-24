package com.jeverbox.bean;

import java.util.ArrayList;
import java.util.List;

import com.jeverbox.EverboxModify;

public class EverboxRunningContext {

	public EverBoxObject preLocalHome;
	public EverBoxObject nowLocalHome;
	/**
	 * 记录当前 本地Home文件夹相当于上一次检查的变化情况
	 */
	public List<EverboxModify> localModifyList = new ArrayList<EverboxModify>();
	
	public EverBoxObject preRemoteHome;
	public EverBoxObject nowRemoteHome;
	
	/**
	 * 记录当前 远程Home文件夹相当于上一次检查的变化情况
	 */
	public List<EverboxModify> remoteModifyList = new ArrayList<EverboxModify>();
	
	/**
	 * 本地Home与远程Home的差异
	 */
	public List<EverboxModify> localRemoteDiffList = new ArrayList<EverboxModify>();
	
	/**
	 * 本地-->远程的操作集合
	 */
	public List<EverboxOPT> remoteOpts = new ArrayList<EverboxOPT>();
	
	/**
	 * 远程-->本地的操作集合
	 */
	public List<EverboxOPT> localOpts = new ArrayList<EverboxOPT>();
}
