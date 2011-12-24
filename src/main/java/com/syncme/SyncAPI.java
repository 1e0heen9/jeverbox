package com.syncme;

import com.syncme.bean.SyncObject;

public interface SyncAPI {
	
	void init() ;

	void login();
	void ping();
	
	SyncObject getRemote(String path);
	
	void download(SyncObject syncObject);
	void upload(SyncObject syncObject);
	
	void mkdirRemote(SyncObject syncObject);
	void deleteRemote(SyncObject syncObject);
	
//	void renameRemote(SyncObject from,SyncObject to);
}
