package com.salama.android.developer.natives;

import com.salama.android.developer.natives.file.FileService;
import com.salama.android.developer.natives.sql.SqlService;

public class SalamaNativeService {

	private static SalamaNativeService _singleton = null;
	
	public static SalamaNativeService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaNativeService();
		}
		
		return _singleton;
	}

	private SalamaNativeService() {
	}
	
	/**
	 * 取得FileService实例
	 * @return FileService实例
	 */
	public FileService getFileService() {
		return FileService.singleton();
	}
	
	/**
	 * 取得SqlService实例
	 * @return SqlService实例
	 */
	public SqlService getSqlService() {
		return SqlService.singleton();
	}

}
