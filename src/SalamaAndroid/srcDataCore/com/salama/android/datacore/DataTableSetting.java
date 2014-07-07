package com.salama.android.datacore;

public class DataTableSetting {
	/**
	 * 表类型 需云同步的表
	 */
	public final static int DATA_TABLE_TYPE_CLOUD_DATA = 0; 

	/**
	 * 表类型 用户信息表
	 */
	public final static int DATA_TABLE_TYPE_USER_DATA = 1;
	
	/**
	 * 表类型 用户自定义表
	 */
	public final static int DATA_TABLE_TYPE_CUSTOMIZE = 2;
	
	private String tableName;
	private int tableType;
	private String primaryKeys;

	/**
	 * 取得表名
	 * @return 表名
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 设置表名
	 * @param tableName 设置表名
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 取得表类型
	 * <BR>值为以下几种常量类型:
	 * <BR>DATA_TABLE_TYPE_CLOUD_DATA,DATA_TABLE_TYPE_USER_DATA,DATA_TABLE_TYPE_CUSTOMIZE
	 * @return 表类型
	 */
	public int getTableType() {
		return tableType;
	}

	/**
	 * 设置表类型
	 * @param tableType 表类型
	 */
	public void setTableType(int tableType) {
		this.tableType = tableType;
	}

	/**
	 * 取得主键信息
	 * <BR>以逗号分隔多个主键的格式. 示例: "id,num,type"
	 * @return
	 */
	public String getPrimaryKeys() {
		return primaryKeys;
	}

	/**
	 * 设置主键信息
	 * @param primaryKeys
	 */
	public void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
}
