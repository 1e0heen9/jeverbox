package com.jeverbox;

import java.io.File;
import java.lang.reflect.Field;

import org.h2.jdbcx.JdbcConnectionPool;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.bean.EverBoxObject;
import com.jeverbox.bean.EverboxDownloadInfo;
import com.jeverbox.bean.FileEditTimeConflict;

/**
 * 用于处理客户端的运行上下文
 * 
 * @author wendal
 */
public class EverboxContext {

	private static final Log log = Logs.get();

	/**
	 * 用于存储持久化数据
	 */
	public static Dao dao = new NutDao(JdbcConnectionPool.create(
			"jdbc:h2:file:everbox", "everbox4j", "everbox4j"));

	static {
		dao.create(FileEditTimeConflict.class, false);
		dao.create(EverboxDownloadInfo.class, false);
	}

	/**
	 * 登记本地文件时间戳冲突
	 * 
	 * @param ebo
	 */
	public static void registerFileConflict(EverBoxObject ebo) {
		log.infof("更新本地文件信息到数据库 %s", ebo.getPath());
		File f = new File(EverboxConfig.getRealPath(ebo));
		if (!f.exists())
			return;
		FileEditTimeConflict fetc = dao.fetch(FileEditTimeConflict.class,
				ebo.getPath());
		if (fetc != null) {
			fetc.setLastModify(ebo.getEditTime());
			fetc.setApiVerion(EverboxConfig.DB_API_VERSION);
			dao.update(fetc);
		} else {
			fetc = new FileEditTimeConflict();
			fetc.setPath(ebo.getPath());
			fetc.setLastModify(ebo.getEditTime());
			fetc.setApiVerion(EverboxConfig.DB_API_VERSION);
			dao.insert(fetc);
		}
		log.info("更新完成");
	}

	/**
	 * 检查时间戳,如果没有冲突就返回true
	 */
	public static boolean checkFileConflict(EverBoxObject ebo) {
		log.infof("向数据库查询文件的伪冲突情况 %s", ebo.getPath());
		File f = new File(EverboxConfig.getRealPath(ebo));
		if (!f.exists())
			return true;
		FileEditTimeConflict fetc = dao.fetch(FileEditTimeConflict.class,
				ebo.getPath());
		if (fetc != null && fetc.getLastModify() == ebo.getEditTime()) {
			// System.out.println(fetc.getLastModify() +" " +
			// ebo.getEditTime());
			return false;
		}
		log.infof("文件不存在伪冲突情况 %s", ebo.getPath());
		return true;
	}

	public static EverboxDownloadInfo getDownloadInfo(String path) {
		return dao.fetch(EverboxDownloadInfo.class, path);
	}

	public static void saveDownloadInfo(EverboxDownloadInfo info) {
		saveOrUpdate(info, "path");
	}

	public static void removeDownloadInfo(EverboxDownloadInfo info) {
		dao.delete(info);
	}

	public static void saveOrUpdate(Object obj, String fieldName) {
		try {
			dao.create(obj.getClass(), false);
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Object d = null;
			if (String.class.equals(field.getType())) {
				String name = (String) field.get(obj);
				d = dao.fetch(obj.getClass(), name);
			} else {
				long id = Long.parseLong(field.get(obj).toString());
				d = dao.fetch(obj.getClass(), id);
			}
			if (d != null) {
				dao.update(obj);
			} else
				dao.insert(obj);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static <T> T get(Class<T> klass, String fieldName, Object value) {
		try {
			dao.create(klass, false);
			Field field = klass.getDeclaredField(fieldName);
			field.setAccessible(true);
			T d = null;
			if (String.class.equals(field.getType())) {
				d = dao.fetch(klass, (String) value);
			} else {
				d = dao.fetch(klass, Long.parseLong(value.toString()));
			}
			return d;
		} catch (Throwable e) {
			log.error("获取出错啦!!", e);
			return null;
		}
	}
}
