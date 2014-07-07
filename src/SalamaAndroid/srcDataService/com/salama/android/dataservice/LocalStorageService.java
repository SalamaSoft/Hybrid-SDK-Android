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

public class LocalStorageService {

	/**
	 * 保存数据 
	 * @param dataTableName 数据表名
	 * @param datas 数据列表
	 * @param extraIndexNames 外部索引字段名(逗号分隔)
	 * @param extraIndexValues 外部索引值(逗号分隔)
	 * @param dbDataUtil DBDataUtil实例
	 * @throws SqliteUtilException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void storeDataToTable(String tableName, List<?> datas, 
			List<String> extraIndexNames, List<String> extraIndexValues, 
			DBDataUtil dbDataUtil) throws SqliteUtilException, IllegalAccessException, InvocationTargetException {
		if(datas == null || datas.size() == 0
				|| tableName == null || tableName.length() == 0) {
			return;
		}
		
		Object dataTmp = null;
		boolean hasExtraIndex = false;
		
		if(extraIndexNames != null && extraIndexNames.size() > 0) {
			hasExtraIndex = true;
		}
		
		List<Object> insertedDatas = new ArrayList<Object>();
		
		for(int i = 0; i < datas.size(); i++) {
			dataTmp = datas.get(i);
			
	        //store data
			try {
				//insert
				dbDataUtil.insertData(tableName, dataTmp);
				
				insertedDatas.add(dataTmp);
			} catch(Exception e) {
				//update
				dbDataUtil.updateDataByPK(tableName, dataTmp);
			}
		}

		//add the extra index row
		if(hasExtraIndex) {
			ExtraIndexManager.insertExtraIndexWithDataTableName(tableName, insertedDatas, 
					extraIndexNames, extraIndexValues, dbDataUtil);
		}
	}
	
	/**
	 * 删除数据
	 * @param dataTableName 数据表名
	 * @param datas 数据列表
	 * @param dbDataUtil DBDataUtil实例
	 * @throws SqliteUtilException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public void removeDataAndExtraIndexForTable(String tableName, List<?> datas,
			DBDataUtil dbDataUtil) throws SqliteUtilException, InvocationTargetException,
			IllegalAccessException {
		if(datas == null || datas.size() == 0
				|| tableName == null || tableName.length() == 0) {
			return;
		}
		
		int i;

		// where conditions of data primary keys ------------------------------------------------------------------
	    //data pk column
		SqliteUtil.SqliteColType sqliteType;
		PropertyDescriptor propertyInfo;
		int k;
		
		DataTableSetting dataTableSetting = dbDataUtil.getDataTableSetting(tableName);
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
		
	    //sql of conditions
		StringBuilder sqlOfWhereConditionPart = new StringBuilder();
		String pkNameTmp = null;
		Object oneDataTmp = null;
		Object propertyValue;
		
		for(k = 0; k < datas.size(); k++) {
			oneDataTmp = datas.get(k);
			
			if(k == 0) {
				sqlOfWhereConditionPart.append(" (");
			} else {
				sqlOfWhereConditionPart.append(" or (");
			}
			
	        //handle one data row ----------------------
			for(i = 0; i < propertyInfoArrayOfPKs.size(); i++) {
				propertyInfo = propertyInfoArrayOfPKs.get(i);
				pkNameTmp = propertyInfo.getName();
				
				propertyValue = propertyInfo.getReadMethod().invoke(oneDataTmp);
				sqliteType = SqliteUtil.getSQLiteColumnTypeByPropertyType(propertyInfo.getPropertyType());
				
				if(sqliteType == SqliteUtil.SqliteColType.SQLITE_TEXT) {
					sqlOfWhereConditionPart.append(" and " + pkNameTmp + " = '" 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)) 
							+ "' ");
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_INTEGER) {
					sqlOfWhereConditionPart.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				} else if(sqliteType == SqliteUtil.SqliteColType.SQLITE_FLOAT) {
					sqlOfWhereConditionPart.append(" and " + pkNameTmp + " = " 
							+ SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				}
			}
			
			sqlOfWhereConditionPart.append(" ) ");
		}
		
	    //delete data rows -----------------------------------------------
		{
			StringBuilder sqlOfDeleteData = new StringBuilder("delete from " + tableName);
			sqlOfDeleteData.append(" where ");
			sqlOfDeleteData.append(sqlOfWhereConditionPart.toString());
			
			dbDataUtil.getSqliteUtil().executeUpdate(sqlOfDeleteData.toString());
		}
		
	    //delete extra index rows ----------------------------------------
		{
			String indexTableName = ExtraIndexManager.getExtraIndexTableNameByDataTableName(tableName);
			StringBuilder sqlOfDeleteExtraIndex = new StringBuilder("delete from " + indexTableName);
			sqlOfDeleteExtraIndex.append(" where ");
			sqlOfDeleteExtraIndex.append(sqlOfWhereConditionPart.toString());

			dbDataUtil.getSqliteUtil().executeUpdate(sqlOfDeleteExtraIndex.toString());
		}
	}
}
