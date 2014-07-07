package com.salama.android.developer.cloud;

import com.salama.android.developer.cloud.file.SalamaCloudFileService;
import com.salama.android.developer.cloud.sql.SalamaCloudSqlService;

public class SalamaCloudService {
	private static SalamaCloudService _singleton = null;
	
	public static SalamaCloudService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaCloudService();
		}
		
		return _singleton;
	}

	private SalamaCloudService() {
	}

	/**
	 * 取得SalamaCloudFileService实例
	 * @return SalamaCloudFileService实例
	 */
	public SalamaCloudFileService getFileService() {
		return SalamaCloudFileService.singleton();
	}
	
	/**
	 * 取得SalamaCloudSqlService实例
	 * @return SalamaCloudSqlService实例
	 */
	public SalamaCloudSqlService getSqlService() {
		return SalamaCloudSqlService.singleton();
	}
}
