package com.salama.android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResourceFileManager {
	private String _storageDirPath;
	
	/**
	 * 设置的存放目录路径
	 * @return 目录路径
	 */
	public String getFileStorageDirPath() {
		return _storageDirPath;
	}

	/**
	 * 构造函数
	 * @param storageDirPath 存放文件的目录路径
	 */
	public ResourceFileManager(String storageDirPath) {
		File storageDir = new File(storageDirPath);

		initStorageDir(storageDir);
	}
	
	/**
	 * 构造函数
	 * @param storageDir 存放文件的目录路径
	 */
	public ResourceFileManager(File storageDir) {
		initStorageDir(storageDir);
	}
	
	private void initStorageDir(File storageDir) {
		String storageDirPath = storageDir.getAbsolutePath();
		
		if(storageDirPath.endsWith(File.separator)) {
			_storageDirPath = storageDirPath.substring(0, storageDirPath.length() - 1);
		} else {
			_storageDirPath = storageDirPath;
		}

		if(storageDir.exists()) {
			if(!storageDir.isDirectory()) {
				storageDir.delete();
				storageDir.mkdirs();
			}
		} else {
			storageDir.mkdirs();
		}
	}
	
	/**
	 * 取得资源文件路径
	 * @param resId 资源文件ID(即文件名)
	 * @return 资源文件路径
	 */
	public String getResourceFilePath(String resId) {
		return _storageDirPath + File.separator + resId;
	}
	
	/**
	 * 资源文件是否存在
	 * @param resId 资源文件ID(即文件名)
	 * @return YES:存在 NO:不存在
	 */
	public boolean isResourceFileExists(String resId) {
		File resFile = new File(_storageDirPath, resId);
		
		return resFile.exists();
	}
	
	/**
	 * 变更资源文件名
	 * @param resId 原文件名
	 * @param resId 新文件名
	 */
	public void changeResId(String resId, String toResId) {
		File resFile = new File(_storageDirPath, resId);
		
		resFile.renameTo(new File(_storageDirPath, toResId));
	}
	
	/**
	 * 保存资源文件
	 * @param data:文件数据
	 * @param resId 文件名
	 */
	public void saveResourceFileWithData(byte[] data, String resId) throws IOException {
		FileOutputStream resFileOS = null;

		try {
			resFileOS = new FileOutputStream(new File(_storageDirPath, resId));
			resFileOS.write(data);
			resFileOS.flush();
		} finally {
			try {
				resFileOS.close();
			} catch(Exception e) {
			}
		}
	}
	
	
}
