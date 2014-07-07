package com.salama.android.datacore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.salama.android.util.SSLog;

import android.util.Log;

import MetoXML.XmlDeserializer;
import MetoXML.Base.XmlParseException;
import MetoXML.Util.PropertyDescriptor;

public class DBDataUtil {
	private SqliteUtil _sqliteUtil;

	private static final String DataTableSettingName = "DataTableSetting";

	/**
	 * 构造函数
	 * @param sqlUtil SqliteUtil实例
	 * @throws SqliteUtilException
	 */
	public DBDataUtil(SqliteUtil sqlUtil) throws SqliteUtilException {
		this._sqliteUtil = sqlUtil;
		
		checkDataTableSetting();
	}
	
	private void checkDataTableSetting() throws SqliteUtilException {
		if(!isTableExists(DataTableSettingName)) {
			createTableDirectly(DataTableSetting.class, "tableName");
		}
	}

	/**
	 * 取得SqliteUtil实例
	 * @return SqliteUtil实例
	 */
	public SqliteUtil getSqliteUtil() {
		return this._sqliteUtil;
	}
	
	/**
	 * 关闭数据库连接
	 */
	public void close() {
		try {
			_sqliteUtil.close();
			_sqliteUtil = null;
		} catch(Exception e) {
		}
	}

	/**
	 * 表是否存在
	 * @param tableName 表名
	 * @return true:存在 false:不存在
	 */
	public boolean isTableExists(String tableName) {

		int count = this._sqliteUtil
				.executeIntScalar("select count(1) from sqlite_master where type='table' and upper(name) = '"
						+ tableName.toUpperCase() + "'; ");
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除表
	 * @param tableName 表名
	 */
	public void dropTable(String tableName) {
		dropTableDirectly(tableName);
		deleteDataTableSetting(tableName);
	}
	
	private void dropTableDirectly(String tableName) {
		_sqliteUtil.executeUpdate("drop table if exists " + tableName);
	}

	/**
	 * 创建表
	 * @param tableCls 表对应的data类型
	 * @param primaryKeys 主键信息。格式为逗号分隔多个主键。
	 * @throws SqliteUtilException
	 */
	public void createTable(Class<?> tableCls, String primaryKeys)
			throws SqliteUtilException {
		createTableDirectly(tableCls, primaryKeys);
		insertDataTableSetting(tableCls.getSimpleName(), primaryKeys);
	}
	
	private void createTableDirectly(Class<?> tableCls, String primaryKeys) 
			throws SqliteUtilException {
		String tableName = tableCls.getSimpleName();
		List<PropertyDescriptor> propertyList = PropertyInfoUtil.getPropertyInfoList(tableCls);

		StringBuilder sql = new StringBuilder();
		sql.append("create table ");
		sql.append(tableName);
		sql.append(" ( ");

		PropertyDescriptor propDesc = null;
		String dbType = "";
		Class<?> colClass = null;
		for (int i = 0; i < propertyList.size(); i++) {
			propDesc = propertyList.get(i);
			colClass = propDesc.getPropertyType();
			if(colClass == String.class) {
				dbType = "TEXT";
			} else if(colClass.isPrimitive()) {
				if(colClass == int.class || colClass == Integer.class
						|| colClass == long.class || colClass == Long.class
						|| colClass == short.class || colClass == Short.class
						|| colClass == byte.class || colClass == Byte.class
				) {
					dbType = "INTEGER";
				} else {
					dbType = "REAL";
				}
			} else {
				dbType = "TEXT";
			}
			
			if (i != 0) {
				sql.append(",");
			}
			sql.append(propDesc.getDisplayName()).append(" ").append(dbType);
		}
		
		if (primaryKeys != null && !"".equals(primaryKeys)) {
			sql.append(",").append(" primary key ")
			.append(" ( ").append(primaryKeys).append(")");
		}
		sql.append(")");

		_sqliteUtil.executeUpdate(sql.toString());
	}
	

	public void createTable(TableDesc tableDesc) throws SqliteUtilException {
		createTableDirectly(tableDesc);
		insertDataTableSetting(tableDesc.getTableName(), tableDesc.getPrimaryKeys());
	}

	private void createTableDirectly(TableDesc tableDesc) {
		String tableName = tableDesc.getTableName();

		StringBuilder sql = new StringBuilder();
		sql.append("create table ");
		sql.append(tableName);
		sql.append(" ( ");

		ColDesc colDesc = null;
		String colType = null;
		String dbType = "";
		for (int i = 0; i < tableDesc.getColDescList().size(); i++) {
			colDesc = tableDesc.getColDescList().get(i);
			colType = colDesc.getColType().toLowerCase();
			
			if(colType.equals("text")) {
				dbType = "TEXT";
			} else if (colType.equals("real")) {
				dbType = "REAL";
			} else {
				dbType = "INTEGER";
			}
			
			if (i != 0) {
				sql.append(",");
			}
			sql.append(colDesc.getColName()).append(" ").append(dbType);
		}
		
		if (tableDesc.getPrimaryKeys() != null && tableDesc.getPrimaryKeys().length() > 0) {
			sql.append(",").append(" primary key ")
			.append(" ( ").append(tableDesc.getPrimaryKeys()).append(")");
		}
		sql.append(")");

		_sqliteUtil.executeUpdate(sql.toString());
	}
	
	/**
	 * 插入记录
	 * @param tableName 表名
	 * @param data 数据实例
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int insertData(String tableName, Object data)
			throws SqliteUtilException {
		List<String> propertyList;
		Object propertyValue = null;
		propertyList = PropertyInfoUtil.getPropertyNameList(data.getClass());

		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(tableName);
		sql.append(" ( ");
		for (int i = 0; i < propertyList.size(); i++) {
			if (i != 0) {
				sql.append(",");
			}
			sql.append(propertyList.get(i));
		}
		sql.append(") ");
		sql.append("values");
		sql.append(" ( ");
		for (int i = 0; i < propertyList.size(); i++) {
			if (i != 0) {
				sql.append(", ");
			}
			sql.append("'");
			propertyValue = getPropertyValue(data,
					propertyList.get(i));
			
			sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));

			sql.append("'");
		}
		sql.append(")");

		return _sqliteUtil.executeUpdate(sql.toString());
	}

	/**
	 * 插入数据
	 * @param tableName 表名
	 * @param dataCls 对应的数据类型
	 * @param dataXml 记录的Xml内容
	 * @return 1:正常 0:出错
	 * @throws IOException
	 * @throws XmlParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws SqliteUtilException
	 */
	public int insertData(String tableName, Class<?> dataCls, String dataXml) throws IOException, XmlParseException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, SqliteUtilException {
		Object data = XmlDeserializer.stringToObject(dataXml, dataCls);
		
		return insertData(tableName, data);
	}

	/**
	 * 插入或更新记录(记录已存在时更新)
	 * @param tableName 表名
	 * @param data 数据实例
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int insertOrUpdateDataByPK(String tableName, Object data)
			throws SqliteUtilException {
		int success = 0;
		try {
			success = insertData(tableName, data);
		} catch (SqliteUtilException e) {
		}

		if(success == 0) {
			try {
				success = updateDataByPK(tableName, data);
			} catch (SqliteUtilException e) {
				Log.e("DBDataUtil", "", e);
			}
		}
		
		return success;
	}

	/**
	 * 根据主键更新记录
	 * @param tableName 表名
	 * @param data 数据实例
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int updateDataByPK(String tableName, Object data)
			throws SqliteUtilException {
		Object propertyValue = null;
		String[] primaryKeysArray = getPrimaryKeySet(tableName);
		List<String> propertyList = PropertyInfoUtil.getPropertyNameList(data.getClass());

		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(tableName);
		sql.append(" set ");
		
		int index = 0;
		for (int i = 0; i < propertyList.size(); i++) {
			if(isInPrimaryKey(primaryKeysArray, propertyList.get(i))) {
				continue;
			}
			
			if (index != 0) {
				sql.append(", ");
			}
			sql.append(propertyList.get(i)).append(" = ").append("'");
			propertyValue = getPropertyValue(data, propertyList.get(i));
			
			sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));

			sql.append("'");
			
			index++;
		}
		if (primaryKeysArray.length > 0) {
			sql.append(" where ");
			for (int i = 0; i < primaryKeysArray.length; i++) {
				if (i != 0) {
					sql.append(" and ");
				}
				sql.append(primaryKeysArray[i]).append(" = ").append("'");
				propertyValue = getPropertyValue(data, primaryKeysArray[i]);
				
				sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));

				sql.append("'");
			}
		}

		return _sqliteUtil.executeUpdate(sql.toString());
	}

	/**
	 * 根据主键更新记录
	 * @param dataCls 数据类型
	 * @param tableName 表名
	 * @param dataXml 数据Xml内容
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int updateDataByPK(Class<?> dataCls, String tableName, String dataXml) 
	throws SqliteUtilException {
		try {
			Object data = XmlDeserializer.stringToObject(dataXml, dataCls);
			return updateDataByPK(tableName, data);
		} catch (IOException e) {
			throw new SqliteUtilException(e);
		} catch (XmlParseException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (InstantiationException e) {
			throw new SqliteUtilException(e);
		} catch (NoSuchMethodException e) {
			throw new SqliteUtilException(e);
		}
		
	}

	/**
	 * 根据主键删除数据
	 * @param tableName 表名
	 * @param data 数据实例
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int deleteDataByPK(String tableName, Object data)
			throws SqliteUtilException {
		Object propertyValue = null;
		String[] primaryKeysArray = getPrimaryKeySet(tableName);

		StringBuilder sql = new StringBuilder();
		sql.append("delete ");
		sql.append(" from ");
		sql.append(tableName);
		if (primaryKeysArray.length > 0) {
			sql.append(" where ");
			for (int i = 0; i < primaryKeysArray.length; i++) {
				if (i != 0) {
					sql.append(" and ");
				}
				sql.append(primaryKeysArray[i]).append(" = ").append("'");
				propertyValue = getPropertyValue(data, primaryKeysArray[i]);
				sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				sql.append("'");
			}
		}

		return _sqliteUtil.executeUpdate(sql.toString());

	}

	/**
	 * 根据主键删除记录
	 * @param dataCls 数据类型
	 * @param tableName 表名
	 * @param dataXml 数据Xml内容
	 * @return 1:正常 0:出错
	 * @throws SqliteUtilException
	 */
	public int deleteDataByPK(Class<?> dataCls, String tableName, String dataXml) throws SqliteUtilException {
		try {
			Object data = XmlDeserializer.stringToObject(dataXml, dataCls);
			return deleteDataByPK(tableName, data);
		} catch (IOException e) {
			throw new SqliteUtilException(e);
		} catch (XmlParseException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (InstantiationException e) {
			throw new SqliteUtilException(e);
		} catch (NoSuchMethodException e) {
			throw new SqliteUtilException(e);
		}
	}

	/**
	 * 删除所有记录
	 * @param tableName 表名
	 * @return 1:正常 0:出错
	 */
	public int deleteAllData(String tableName) {

		StringBuilder sql = new StringBuilder();
		sql.append("delete ").append(" from ").append(tableName);

		return _sqliteUtil.executeUpdate(sql.toString());
	}

	/**
	 * 根据主键查询
	 * @param tableName 表名
	 * @param data 数据实例(只需包含主键信息)
	 * @return 查询结果数据实例
	 * @throws SqliteUtilException
	 */
	public Object findDataByPK(String tableName, Object data)
			throws SqliteUtilException {
		Object propertyValue = null;
		//List<String> propertyList = PropertyInfoUtil.getPropertyNameList(data.getClass());
		String[] primaryKeysArray = getPrimaryKeySet(tableName);

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		/*
		for (int i = 0; i < propertyList.size(); i++) {
			if (i != 0) {
				sql.append(", ");
			}
			sql.append(propertyList.get(i));
		}
		*/
		sql.append(" from ");
		sql.append(tableName);
		if (primaryKeysArray.length > 0) {
			sql.append(" where ");
			for (int i = 0; i < primaryKeysArray.length; i++) {
				if (i != 0) {
					sql.append(" and ");
				}
				sql.append(primaryKeysArray[i]).append(" = ").append("'");
				propertyValue = getPropertyValue(data, primaryKeysArray[i]);
				//SSLog.d("DBDataUtil", "findDataByPK() pk:" + primaryKeysArray[i]);
				sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				sql.append("'");
			}
		}


		return _sqliteUtil.findData(sql.toString(), data.getClass());

	}

	/**
	 * 根据主键查询
	 * @param dataCls 数据类型
	 * @param tableName 表名
	 * @param dataXml 数据Xml内容
	 * @return 查询结果数据实例
	 * @throws SqliteUtilException
	 */
	public Object findDataByPK(Class<?> dataCls, String tableName, String dataXml) throws SqliteUtilException {
		try {
			Object data = XmlDeserializer.stringToObject(dataXml, dataCls);
			return findDataByPK(tableName, data);
		} catch (IOException e) {
			throw new SqliteUtilException(e);
		} catch (XmlParseException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (InstantiationException e) {
			throw new SqliteUtilException(e);
		} catch (NoSuchMethodException e) {
			throw new SqliteUtilException(e);
		}
	}

	/**
	 * 根据主键查询数据
	 * @param tableName 表名
	 * @param data 数据实例
	 * @return 数据Xml内容
	 * @throws SqliteUtilException
	 */
	public String findDataXmlByPK(String tableName, Object data)
			throws SqliteUtilException {
		Object propertyValue = null;
		//List<String> propertyList = PropertyInfoUtil.getPropertyNameList(data.getClass());
		String[] primaryKeysArray = getPrimaryKeySet(tableName);

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		/*
		for (int i = 0; i < propertyList.size(); i++) {
			if (i != 0) {
				sql.append(", ");
			}
			sql.append(propertyList.get(i));

		}
		*/
		sql.append(" from ");
		sql.append(tableName);
		if (primaryKeysArray.length > 0) {
			sql.append(" where ");
			for (int i = 0; i < primaryKeysArray.length; i++) {
				if (i != 0) {
					sql.append(" and ");
				}
				sql.append(primaryKeysArray[i]).append(" = ").append("'");
				propertyValue = getPropertyValue(data, primaryKeysArray[i]);
				sql.append(SqliteUtil.encodeQuoteChar(SqliteUtil.convertObjectToString(propertyValue)));
				sql.append("'");
			}
		}


		return _sqliteUtil.findDataXml(sql.toString(), data.getClass());

	}

	/**
	 * 根据主键查询
	 * @param dataCls 数据类型
	 * @param tableName 表名
	 * @param dataXml 数据Xml内容(只需包含主键信息)
	 * @return 数据Xml内容
	 * @throws SqliteUtilException
	 */
	public String findDataXmlByPK(Class<?> dataCls, String tableName, String dataXml) throws SqliteUtilException {
		try {
			Object data = XmlDeserializer.stringToObject(dataXml, dataCls);
			return findDataXmlByPK(tableName, data);
		} catch (IOException e) {
			throw new SqliteUtilException(e);
		} catch (XmlParseException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (InstantiationException e) {
			throw new SqliteUtilException(e);
		} catch (NoSuchMethodException e) {
			throw new SqliteUtilException(e);
		}
	}

	/**
	 * 查询所有数据
	 * @param tableCls 数据类型
	 * @return 数据列表
	 * @throws SqliteUtilException
	 */
	public List<?> findAllData(Class<?> tableCls) throws SqliteUtilException {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ").append(tableCls.getSimpleName());

		return _sqliteUtil.findDataList(sql.toString(), tableCls);

	}

	/**
	 * 查询所有数据
	 * @param tableCls 数据类型
	 * @return 数据列表Xml内容
	 */
	public String findAllDataXml(Class<?> tableCls) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ").append(tableCls.getSimpleName());

		return _sqliteUtil.findDataListXml(sql.toString(), tableCls);
	}

	/**
	 * 查询更新时间晚于指定时间的数据
	 * @param tableCls 数据类型
	 * @param updateTime 指定的时间
	 * @return 数据列表
	 * @throws SqliteUtilException
	 */
	public List<?> findDataAfterUpdateTime(Class<?> tableCls, long updateTime)
			throws SqliteUtilException {
		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append(" from ");
		sql.append(tableCls.getSimpleName());
		sql.append(" where updateTime = '").append(String.valueOf(updateTime)).append("'");

		return _sqliteUtil.findDataList(sql.toString(), tableCls);

	}

	/**
	 * 查询更新时间晚于指定时间的数据
	 * @param tableCls 数据类型
	 * @param updateTime 指定的时间
	 * @return 数据列表Xml内容
	 * @throws SqliteUtilException
	 */
	public String findDataXmlAfterUpdateTime(Class<?> tableCls, long updateTime)
			throws SqliteUtilException {
		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append(" from ");
		sql.append(tableCls.getSimpleName());
		sql.append(" where updateTime = '").append(String.valueOf(updateTime)).append("'");

		return _sqliteUtil.findDataListXml(sql.toString(), tableCls);

	}
	
	/**
	 * 获取表定义
	 * @param tableName 表名
	 * @return 表定义
	 * @throws SqliteUtilException
	 */
	public DataTableSetting getDataTableSetting(String tableName) throws SqliteUtilException {
		String sql = "select * from DataTableSetting where tableName = '" + tableName + "'";
		return (DataTableSetting) _sqliteUtil.findData(sql, DataTableSetting.class);
	}
	
	private void insertDataTableSetting(String tableName, String primaryKeys) throws SqliteUtilException {
		DataTableSetting tableSetting = new DataTableSetting();
		
		tableSetting.setTableName(tableName);
		tableSetting.setTableType(DataTableSetting.DATA_TABLE_TYPE_CUSTOMIZE);
		tableSetting.setPrimaryKeys(primaryKeys);
		
		insertData(DataTableSettingName, tableSetting);
	}
	
	private void deleteDataTableSetting(String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(" delete DataTableSetting from where tableName = '").append(tableName).append("'");
		
		_sqliteUtil.executeUpdate(sql.toString());
	}

	private String[] getPrimaryKeySet(String tableName) {
		StringBuilder sql = new StringBuilder();
		sql.append(" select ");
		sql.append("  primaryKeys ");
		sql.append(" from ");
		sql.append(" DataTableSetting ");
		sql.append(" where tableName = ").append(" '").append(tableName).append("'");

		String primaryKeys = _sqliteUtil.executeStringScalar(sql.toString());
		
		return primaryKeys.split(",");
	}
	
	private boolean isInPrimaryKey(String[] primaryKeysArray, String colName) {
		for(int i = 0; i < primaryKeysArray.length; i++) {
			if(primaryKeysArray[i].equalsIgnoreCase(colName)) {
				return true;
			}
		}
		
		return false;
	}

	private Object getPropertyValue(Object data,
			String properyName) throws SqliteUtilException {
		Method getMethod = null;
		Object value = null;
		PropertyDescriptor pd;
		try {
			pd = new PropertyDescriptor(properyName, data.getClass());
			getMethod = pd.getReadMethod();
		} catch (NoSuchMethodException e1) {
			return null;
		}

		try {
			if (getMethod != null) {
				value = getMethod.invoke(data);
			}
		} catch (IllegalArgumentException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		}
		return value;
	}
	
}
