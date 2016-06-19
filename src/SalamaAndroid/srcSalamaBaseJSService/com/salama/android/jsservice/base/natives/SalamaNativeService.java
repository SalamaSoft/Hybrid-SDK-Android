package com.salama.android.jsservice.base.natives;

import com.salama.android.dataservice.SalamaDataService;
import com.salama.android.jsservice.base.natives.file.FileService;
import com.salama.android.jsservice.base.natives.sql.SqlService;

public class SalamaNativeService {
	private final FileService _fileService;
	private final SqlService _sqlService;

	public SalamaNativeService(SalamaDataService dataService) {
		_fileService = new FileService();
		_sqlService = new SqlService(dataService);
	}
	
	/**
	 * 取得FileService实例
	 * @return FileService实例
	 */
	public FileService getFileService() {
		return _fileService;
	}
	
	/**
	 * 取得SqlService实例
	 * @return SqlService实例
	 */
	public SqlService getSqlService() {
		return _sqlService;
	}

}
