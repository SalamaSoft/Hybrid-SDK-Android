package com.salama.android.datacore;

import java.util.ArrayList;
import java.util.List;

public class TableDesc {
	private String tableName;
	
	private String primaryKeys;
	
	private List<ColDesc> colDescList = new ArrayList<ColDesc>();

	public String getTableName() {
		return tableName;
	}

	/**
	 * 
	 * @param tableName 表名。因框架中提供Xml<->Data类的反射工具，建议类似"UserData","CompanyData"，这样的Data类名的形式。
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPrimaryKeys() {
		return primaryKeys;
	}

	/**
	 * 
	 * @param primaryKeys 主键描述，格式:逗号分隔的列名。
	 */
	public void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public List<ColDesc> getColDescList() {
		return colDescList;
	}

	/**
	 * 
	 * @param colDescList 字段描述，格式:NSArray<ColDesc>。
	 */
	public void setColDescList(List<ColDesc> colDescList) {
		this.colDescList = colDescList;
	}
	
	
}
