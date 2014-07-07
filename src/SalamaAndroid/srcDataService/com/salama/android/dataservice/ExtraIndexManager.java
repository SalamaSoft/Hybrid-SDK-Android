package com.salama.android.dataservice;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import MetoXML.Util.PropertyDescriptor;

import com.salama.android.datacore.DBDataUtil;
import com.salama.android.datacore.DataTableSetting;
import com.salama.android.datacore.PropertyInfoUtil;
import com.salama.android.datacore.SqliteUtil;
import com.salama.android.datacore.SqliteUtilException;

public class ExtraIndexManager {
	/**
	 * 外部索引表名前缀
	 */
	public static final String PREFIX_SETTING_TABLE_NAME = "ExtraIndex";

	/**
	 * 取得外部索引表名
	 * @param dataTableName 数据表名
	 * @return 外部索引表名 
	 */
	public static String getExtraIndexTableNameByDataTableName(String dataTableName) {
		return PREFIX_SETTING_TABLE_NAME + dataTableName;
	}
	
	/**
	 * 创建外部索引表
	 * @param dataTableName 数据表名
	 * @param dataPrimaryKeys 数据表主键(逗号分隔)
	 * @param extraIndexes 外部索引字段(逗号分隔)
	 * @param dataClass 数据类型
	 * @param dbDataUtil DBDataUtil实例
	 */
	public static void createExtraIndexTableWithDataTableName(String dataTableName, String dataPrimaryKeys,
			String extraIndexes, Class<?> dataClass, DBDataUtil dbDataUtil) {
		int i;
		StringBuilder sql = new StringBuilder();
		String indexTableName = getExtraIndexTableNameByDataTableName(dataTableName);
		
		sql.append("create table " + indexTableName + " (");
		
		String[] indexNameArray = extraIndexes.split(",");
		for(i = 0; i < indexNameArray.length; i++) {
			sql.append(indexNameArray[i] + " TEXT,");
		}
		
	    //data pk column
		String[] pkNameArray = dataPrimaryKeys.split(",");
		
		List<PropertyDescriptor> propertyArray = PropertyInfoUtil.getPropertyInfoList(dataClass);
		
		SqliteUtil.SqliteColType sqliteColumnType;
		boolean isPK;
		int k;
		PropertyDescriptor propertyInfo;
		
		for(i = 0; i < propertyArray.size(); i++) {
			propertyInfo = propertyArray.get(i);
			
			isPK = false;
			for(k = 0; k < pkNameArray.length; k++) {
				if(pkNameArray[k].equalsIgnoreCase(propertyInfo.getName())) {
					isPK = true;
					break;
				}
			}
		
			if(!isPK) {
				break;
			}
			
			sqliteColumnType = SqliteUtil.getSQLiteColumnTypeByPropertyType(propertyInfo.getClass());
			
			if(sqliteColumnType == SqliteUtil.SqliteColType.SQLITE_TEXT) {
				sql.append(propertyInfo.getName() + " TEXT,");
			} else if (sqliteColumnType == SqliteUtil.SqliteColType.SQLITE_INTEGER) {
				sql.append(propertyInfo.getName() + " INTEGER,");
			} else if (sqliteColumnType == SqliteUtil.SqliteColType.SQLITE_FLOAT) {
				sql.append(propertyInfo.getName() + " REAL,");
			}
			
		}
		
	    //primary key for this table
		StringBuilder pkOfThisTable = new StringBuilder();
		pkOfThisTable.append(extraIndexes);
		if(!extraIndexes.endsWith(",") && !dataPrimaryKeys.startsWith(",")) {
			pkOfThisTable.append(",");
		} 
		pkOfThisTable.append(dataPrimaryKeys);
		
		
		sql.append(" primary key(" + pkOfThisTable + ")");
		
	    //end of create table
		sql.append(")");
		
	    //Execute the sql
		dbDataUtil.getSqliteUtil().executeUpdate(sql.toString());
	}
	
	/**
	 * 删除外部索引表名
	 * @param dataTableName 数据表名
	 * @param dbDataUtil DBDataUtil实例
	 */
	public static void dropExtraIndexTableByDataTableName(String dataTableName, DBDataUtil dbDataUtil) {
		dbDataUtil.dropTable(getExtraIndexTableNameByDataTableName(dataTableName));
	}
	
	/**
	 * 插入外部索引记录
	 * @param dataTableName 数据表名
	 * @param datas 数据列表
	 * @param extraIndexNames 外部索引字段名(逗号分隔)
	 * @param extraIndexValues 外部索引值(逗号分隔)
	 * @param dbDataUtil DBDataUtil实例
	 * @throws SqliteUtilException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static void insertExtraIndexWithDataTableName(String dataTableName, List<?> datas, 
			List<String> extraIndexNames, List<String> extraIndexValues, DBDataUtil dbDataUtil) 
					throws SqliteUtilException, InvocationTargetException, IllegalAccessException {
		if(datas == null || datas.size() == 0) {
			return;
		}
		
		int i;
		
	    //create sql format -------------------------------------------------
		StringBuilder sqlFormat = new StringBuilder();
		
	    //beginning of sql
		sqlFormat.append("insert into " + getExtraIndexTableNameByDataTableName(dataTableName) + " (");

		//columns of extra indexes
		sqlFormat.append(" ," + extraIndexNames.get(0));
		for(i = 1; i < extraIndexNames.size(); i++) {
			sqlFormat.append(" ," + extraIndexNames.get(i));
		}
		
	    //columns of data primary keys
		DataTableSetting dataTableSetting = dbDataUtil.getDataTableSetting(dataTableName);
		String[] dataPkArray = dataTableSetting.getPrimaryKeys().split(",");
		for(i = 0; i < dataPkArray.length; i++) {
			sqlFormat.append(" ," + dataPkArray[i]);
		}
		
	    // end of setting columns ----
		sqlFormat.append(") values (");
		
	    //value of extra indexes
		sqlFormat.append(" '" + SqliteUtil.encodeQuoteChar(extraIndexValues.get(0)) + "' ");
		for(i = 1; i < extraIndexValues.size(); i++) {
			sqlFormat.append(",'" + SqliteUtil.encodeQuoteChar(extraIndexValues.get(i)) + "' ");
		}
		
	    // set values of primary key ------------------------------------------------------------------
	    //data pk column
		SqliteUtil.SqliteColType sqliteType;
		PropertyDescriptor propertyInfo;
		int k;
		
		List<PropertyDescriptor> propertyInfoArrayOfDataClass = 
				PropertyInfoUtil.getPropertyInfoList(datas.get(0).getClass());
		List<PropertyDescriptor> propertyInfoArrayOfPKs = new ArrayList<PropertyDescriptor>();
		
		for(i = 0; i < dataPkArray.length; i++) {
			for(k = 0; k < propertyInfoArrayOfDataClass.size(); k++) {
				propertyInfo = propertyInfoArrayOfDataClass.get(k);
				
				if(propertyInfo.getName().equalsIgnoreCase(dataPkArray[i])) {
					//pk
					propertyInfoArrayOfPKs.add(propertyInfo);
					break;
				}
				
			}
		}
		
		//sql
		int sqlPrefixLength = sqlFormat.length();
		Object oneDataTmp = null;
		Object propertyValue = null;
		
		for(k = 0; k < datas.size(); k++) {
			if(sqlFormat.length() > sqlPrefixLength) {
				sqlFormat.delete(sqlPrefixLength, sqlFormat.length());
			}
			
			oneDataTmp = datas.get(k);
			
	        //handle one data row ----------------------
			for(i = 0; i < propertyInfoArrayOfPKs.size(); i++) {
				propertyInfo = propertyInfoArrayOfPKs.get(i);
				propertyValue = propertyInfo.getReadMethod().invoke(oneDataTmp);
				
				sqliteType = SqliteUtil.getSQLiteColumnTypeByPropertyType(propertyInfo.getPropertyType());
				if(sqliteType == SqliteUtil.SqliteColType.SQLITE_TEXT) {
					sqlFormat.append(",'" + SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)) + "' ");
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_INTEGER) {
					sqlFormat.append("," + SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_FLOAT) {
					sqlFormat.append("," + SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				}
			}
			
	        //end of insert sql
			sqlFormat.append(")");
			
	        //execute
			dbDataUtil.getSqliteUtil().executeUpdate(sqlFormat.toString());
		}
	}
	
	/**
	 * 删除外部索引记录
	 * @param dataTableName 数据表名
	 * @param datas 数据列表
	 * @param extraIndexNames 外部索引字段名(逗号分隔)
	 * @param extraIndexValues 外部索引值(逗号分隔)
	 * @param dbDataUtil DBDataUtil实例
	 * @throws SqliteUtilException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static void deleteExtraIndexByDataTableName(String dataTableName, List<?> datas, 
			List<String> extraIndexNames, List<String> extraIndexValues, DBDataUtil dbDataUtil) 
					throws SqliteUtilException, InvocationTargetException, IllegalAccessException {
		if(datas == null || datas.size() == 0) {
			return;
		}

		
		int i;
		StringBuilder sqlFormat = new StringBuilder();
		String indexTableName = getExtraIndexTableNameByDataTableName(dataTableName);
		
		sqlFormat.append("delete from " + indexTableName);
		sqlFormat.append(" where ");

	    //conditions of extra index
		sqlFormat.append(" " + extraIndexNames.get(0) + " = '" 
			+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(extraIndexValues.get(0))) + "' ");
		for(i = 1; i < extraIndexNames.size(); i++) {
			sqlFormat.append(" and " + extraIndexNames.get(i) + " = '" 
				+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(extraIndexValues.get(i))) 
				+ "' ");
		}
		
	    // set values of primary key ------------------------------------------------------------------
	    //data pk column
		SqliteUtil.SqliteColType sqliteType;
		PropertyDescriptor propertyInfo;
		int k;
		
		DataTableSetting dataTableSetting = dbDataUtil.getDataTableSetting(dataTableName);
		String[] dataPkArray = dataTableSetting.getPrimaryKeys().split(",");

		List<PropertyDescriptor> propertyInfoArrayOfDataClass = 
				PropertyInfoUtil.getPropertyInfoList(datas.get(0).getClass());
		List<PropertyDescriptor> propertyInfoArrayOfPKs = new ArrayList<PropertyDescriptor>();
		
		for(i = 0; i < dataPkArray.length; i++) {
			for(k = 0; k < propertyInfoArrayOfDataClass.size(); k++) {
				propertyInfo = propertyInfoArrayOfDataClass.get(k);
				
				if(propertyInfo.getName().equalsIgnoreCase(dataPkArray[i])) {
					//pk
					propertyInfoArrayOfPKs.add(propertyInfo);
					break;
				}
				
			}
		}
		
	    //sql
		sqlFormat.append(" and (");
		
		String pkNameTmp = null;
		Object oneDataTmp = null;
		Object propertyValue = null;
		
		for(k = 0; k < datas.size(); k++) {
			oneDataTmp = datas.get(k);
			
			if(k == 0) {
				sqlFormat.append(" (");
			} else {
				sqlFormat.append(" or (");
			}
			
	        //handle one data row ----------------------
			for(i = 0; i < propertyInfoArrayOfPKs.size(); i++) {
				propertyInfo = propertyInfoArrayOfPKs.get(i);
				pkNameTmp = dataPkArray[i];

				propertyValue = propertyInfo.getReadMethod().invoke(oneDataTmp);
				
				sqliteType = SqliteUtil.getSQLiteColumnTypeByPropertyType(propertyInfo.getPropertyType());
				if(sqliteType == SqliteUtil.SqliteColType.SQLITE_TEXT) {
					sqlFormat.append(" and " + pkNameTmp + " = '" 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)) 
							+ "' ");
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_INTEGER) {
					sqlFormat.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_FLOAT) {
					sqlFormat.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				}
			}
			
			sqlFormat.append(" ) ");
		}
		
		sqlFormat.append(")");
		dbDataUtil.getSqliteUtil().executeUpdate(sqlFormat.toString());
	}
	
	/**
	 * 删除外部索引记录 
	 * @param dataTableName 数据表名
	 * @param datas 数据列表
	 * @param dbDataUtil DBDataUtil实例
	 * @throws SqliteUtilException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static void deleteExtraIndexByDataTableName(String dataTableName, List<?> datas, 
			DBDataUtil dbDataUtil) throws SqliteUtilException, InvocationTargetException, IllegalAccessException {
		if(datas == null || datas.size() == 0) {
			return;
		}

		
		int i;
		StringBuilder sqlFormat = new StringBuilder();
		String indexTableName = getExtraIndexTableNameByDataTableName(dataTableName);
		
		sqlFormat.append("delete from " + indexTableName);
		sqlFormat.append(" where ");
		
	    // set values of primary key ------------------------------------------------------------------
	    //data pk column
		SqliteUtil.SqliteColType sqliteType;
		PropertyDescriptor propertyInfo;
		int k;
		
		DataTableSetting dataTableSetting = dbDataUtil.getDataTableSetting(dataTableName);
		String[] dataPkArray = dataTableSetting.getPrimaryKeys().split(",");

		List<PropertyDescriptor> propertyInfoArrayOfDataClass = 
				PropertyInfoUtil.getPropertyInfoList(datas.get(0).getClass());
		List<PropertyDescriptor> propertyInfoArrayOfPKs = new ArrayList<PropertyDescriptor>();
		
		for(i = 0; i < dataPkArray.length; i++) {
			for(k = 0; k < propertyInfoArrayOfDataClass.size(); k++) {
				propertyInfo = propertyInfoArrayOfDataClass.get(k);
				
				if(propertyInfo.getName().equalsIgnoreCase(dataPkArray[i])) {
					//pk
					propertyInfoArrayOfPKs.add(propertyInfo);
					break;
				}
				
			}
		}
		
	    //sql
		String pkNameTmp = null;
		Object oneDataTmp = null;
		Object propertyValue = null;
		
		for(k = 0; k < datas.size(); k++) {
			oneDataTmp = datas.get(k);
			
			if(k == 0) {
				sqlFormat.append(" (");
			} else {
				sqlFormat.append(" or (");
			}
			
	        //handle one data row ----------------------
			for(i = 0; i < propertyInfoArrayOfPKs.size(); i++) {
				propertyInfo = propertyInfoArrayOfPKs.get(i);
				pkNameTmp = dataPkArray[i];

				propertyValue = propertyInfo.getReadMethod().invoke(oneDataTmp);
				
				sqliteType = SqliteUtil.getSQLiteColumnTypeByPropertyType(propertyInfo.getPropertyType());
				if(sqliteType == SqliteUtil.SqliteColType.SQLITE_TEXT) {
					sqlFormat.append(" and " + pkNameTmp + " = '" 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)) 
							+ "' ");
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_INTEGER) {
					sqlFormat.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_FLOAT) {
					sqlFormat.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				}
			}
			
			sqlFormat.append(" ) ");
		}
		
		dbDataUtil.getSqliteUtil().executeUpdate(sqlFormat.toString());
	}
}
