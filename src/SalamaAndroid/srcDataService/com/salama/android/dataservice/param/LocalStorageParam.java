package com.salama.android.dataservice.param;

import java.util.ArrayList;
import java.util.List;

public class LocalStorageParam {
	private String tableName = "";
	
	private String dataClass = "";
	
	private List<String> extraIndexNames = new ArrayList<String>();
	
	private List<String> extraIndexValues = new ArrayList<String>();

	/**
	 * 取得表名
	 * @return 表名
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 设置表名
	 * @param tableName 表名
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 取得数据类型
	 * @return 数据类型
	 */
	public String getDataClass() {
		return dataClass;
	}

	/**
	 * 设置数据类型
	 * @param dataClass 数据类型
	 */
	public void setDataClass(String dataClass) {
		this.dataClass = dataClass;
	}

	/**
	 * 取得外部索引字段名列表
	 * @return 外部索引字段名列表
	 */
	public List<String> getExtraIndexNames() {
		return extraIndexNames;
	}

	/**
	 * 设置外部索引字段名列表
	 * @param extraIndexNames 外部索引字段名列表
	 */
	public void setExtraIndexNames(List<String> extraIndexNames) {
		this.extraIndexNames = extraIndexNames;
	}

	/**
	 * 取得外部索引字段值列表
	 * @return 外部索引字段值列表
	 */
	public List<String> getExtraIndexValues() {
		return extraIndexValues;
	}

	/**
	 * 设置外部索引字段值列表
	 * @param extraIndexNames 外部索引字段值列表
	 */
	public void setExtraIndexValues(List<String> extraIndexValues) {
		this.extraIndexValues = extraIndexValues;
	}
	
}
