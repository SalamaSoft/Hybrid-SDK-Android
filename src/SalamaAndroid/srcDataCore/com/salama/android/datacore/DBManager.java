package com.salama.android.datacore;

import java.io.File;

import com.salama.android.util.SSLog;

import android.content.Context;
import android.util.Log;

public class DBManager {
	private String _dbFilePath;

	/*
	 * deprecated public static String defaultDbDirPath(Context context){
	 * //return
	 * getApplicationContext().getDatabasePath(Consts.LOCAL_DB).getAbsolutePath
	 * (); return ""; }
	 */

	/**
	 * 构造函数
	 * @param context 上下文
	 * @param dbName 数据库文件名
	 */
	public DBManager(Context context, String dbName) {
		File dbFile = context.getApplicationContext().getDatabasePath(dbName);
		_dbFilePath = dbFile.getAbsolutePath();
		
		SSLog.i("DBManager", "dbFilePath:" + _dbFilePath);
		
		File dir = dbFile.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * 创建DBDataUtil
	 * @return DBDataUtil
	 * @throws SqliteUtilException
	 */
	public DBDataUtil createNewDBDataUtil() throws SqliteUtilException {
		SqliteUtil sqlUtil = new SqliteUtil(_dbFilePath);
		sqlUtil.open();

		DBDataUtil dbDataUtil = new DBDataUtil(sqlUtil);

		return dbDataUtil;
	}
}
