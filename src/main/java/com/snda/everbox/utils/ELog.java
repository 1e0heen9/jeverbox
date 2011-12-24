package com.snda.everbox.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


//经过修改,移除对Android的依赖
public class ELog {

	private static final String MY = ELog.class.getName();

	public static void e(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Logger.getLogger(ste.getClassName()).log(MY, Level.ERROR, msg, null);
	    
	}

	public static void i(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Logger.getLogger(ste.getClassName()).log(MY, Level.INFO, msg, null);
	}
	
	public static void d(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		Logger.getLogger(ste.getClassName()).log(MY, Level.DEBUG, msg, null);
	}
}