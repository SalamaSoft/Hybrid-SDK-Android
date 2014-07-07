package com.salama.android.developer.cloud.file;

public class FileOperateResult {

	private String fileId;
	private int success;
	
	/**
	 * fileId
	 */
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	/**
	 * 成功标识(1:成功 0:失败)
	 */
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	
}
