package com.salama.android.datacore;

public class ColDesc {

	private String colName;
	
	private String colType;

	public String getColName() {
		return colName;
	}

	/**
	 * 
	 * @param colName 列名
	 * 因框架中提供Xml<->Data反射工具，建议大小写敏感，例如"dataId","userName"，这样的典型的data属性名的形式。注意，如果在ios中定义Data类用于反射，则字段名需须避开关键字，诸如,"id","newXXX"。
	 */
	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getColType() {
		return colType;
	}

	/**
	 * 
	 * @param colType 列类型
	 * 鉴于SQLITE中实际存在3种列类型：TEXT,INTEGER,REAL。
	 * 所以，此处colType仅支持:
	 * "text"(不区分大小写):对应TEXT类型。
	 * "int","integer"(不区分大小写):对应INTEGER类型
	 * "real"(不区分大小写):对应REAL类型
	 */
	public void setColType(String colType) {
		this.colType = colType;
	}
	
	public ColDesc() {
	}
	
	public ColDesc(String name, String type) {
		this.colName = name;
		this.colType = type;
	}
}
