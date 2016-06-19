package com.salama.android.jsservice.base.natives.sql;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import MetoXML.XmlDeserializer;
import MetoXML.XmlReader;
import MetoXML.Base.XmlNode;
import android.util.Log;

import com.salama.android.datacore.DBDataUtil;
import com.salama.android.datacore.SqliteUtil;
import com.salama.android.datacore.SqliteUtilException;
import com.salama.android.datacore.TableDesc;
import com.salama.android.dataservice.SalamaDataService;

public class SqlService {
	private final SalamaDataService _dataService;
	private ConcurrentHashMap<String, String> _colTypeMapping = new ConcurrentHashMap<String, String>();

	public SqlService(SalamaDataService dataService) {
		_dataService = dataService;
	}
	
	/**
	 * 判断表是否已经存在
	 * @return 0:不存在 1:存在
	 */
	public int isTableExists(String tableName) {
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			return dbDataUtil.isTableExists(tableName)?1:0;
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "isTableExists()", e);
			return 0;
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 建表(如果表已经存在，则不做任何事)
	 * @param tableDesc 表结构描述
	 * @return 表名
	 */
	public String createTable(TableDesc tableDesc) {
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			if(!dbDataUtil.isTableExists(tableDesc.getTableName())) {
				dbDataUtil.createTable(tableDesc);
			}
			
			return tableDesc.getTableName();
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "createTable()", e);
			return tableDesc.getTableName();
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 删表
	 * @param tableName 表名
	 * @return 表名
	 */
	public String dropTable(String tableName) {
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			dbDataUtil.dropTable(tableName);
			
			return tableName;
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "dropTable()", e);
			return tableName;
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 执行查询语句
	 * @param sql sql文
	 * @return 查询结果(XML格式。例:<List><TestData>...</TestData><TestData>...</TestData>......</List>)
	 */
	public String executeQuery(String sql, String dataNodeName) {
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			return dbDataUtil.getSqliteUtil().findDataListXml(sql, dataNodeName);
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "executeQuery()", e);
			return "";
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 执行更新语句(update或delete)
	 * @param sql sql文
	 * @return 1:成功 0:失败
	 */
	public int executeUpdate(String sql) {
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			return dbDataUtil.getSqliteUtil().executeUpdate(sql);
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "executeUpdate()", e);
			return 0;
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 插入数据
	 * @param dataTable 表名
	 * @param dataXml 数据XML
	 * @return dataXml 数据XML。失败的场合，返回nil。
	 */
	public String insertData(String dataTable, String dataXml) {
	    //make sql ------------------
		StringBuilder sqlColNamesPart = new StringBuilder();
		StringBuilder sqlColValuesPart = new StringBuilder();

		sqlColNamesPart.append("(");
		sqlColValuesPart.append("(");
		
		try {
			XmlReader xmlReader = new XmlReader();
			XmlNode xmlRootNode = xmlReader.StringToXmlNode(dataXml, XmlDeserializer.DefaultCharset);
			XmlNode nodeTmp = null;
			
			nodeTmp = xmlRootNode.getFirstChildNode();
			
	        int colIndex = 0;
			String nodeName = null;
			String nodeValue = null;
			String colType = null;
			while(nodeTmp != null) {
				nodeName = nodeTmp.getName();
				nodeValue = nodeTmp.getContent();
				
	            //col name
	            if(colIndex != 0) {
	            	sqlColNamesPart.append(",");
	            	sqlColValuesPart.append(",");
	            }
	            sqlColNamesPart.append(nodeName);
	            
	            //col type
	            colType = getColTypeOfTable(dataTable, nodeName);
	            if(colType == null) {
	            	throw new RuntimeException("Column " + nodeName + " maybe not exists in table " + dataTable);
	            } else {
		            if(colType.equals("text")) {
		            	sqlColValuesPart.append("'").append(SqliteUtil.encodeQuoteChar(nodeValue)).append("'");
		            } else {
		            	sqlColValuesPart.append(nodeValue);
		            }
	            } 
	            
				nodeTmp = nodeTmp.getNextNode();
				colIndex ++;
			}
		} catch(Exception e) {
			Log.e("SqlService", "insertData()", e);
			return null;
		}
		
		sqlColNamesPart.append(")");
		sqlColValuesPart.append(")");
		
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ").append(dataTable)
		.append(" ").append(sqlColNamesPart.toString())
		.append(" values ").append(sqlColValuesPart);
		
	    //execute sql ------------------
		DBDataUtil dbDataUtil = null;
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			int success = dbDataUtil.getSqliteUtil().executeUpdate(sql.toString());
			if(success != 0) {
				return dataXml;
			} else {
				return null;
			}
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "insertData()", e);
			return null;
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
	}
	
	private String getColTypeMappingKeyOfTable(String table, String colName) {
		return table.toLowerCase().concat(".").concat(colName.toLowerCase());
	}
	
	private void setColTypeOfTable(String table, String colName, String colType) {
		String colTypeLower = colType.toLowerCase();
		if(colTypeLower.equals("text") || colTypeLower.equals("integer") || colTypeLower.equals("real")) {
			String key = getColTypeMappingKeyOfTable(table, colName);
			_colTypeMapping.put(key, colTypeLower);
		}
	}
	
	private String getColTypeOfTable(String table, String colName) {
		String key = getColTypeMappingKeyOfTable(table, colName);
		String colType = _colTypeMapping.get(key);
		
		if(colType == null) {
			loadColTypesOfTable(table);
			
			return _colTypeMapping.get(key);
		} else {
			return colType;
		}
	}
	
	private void loadColTypesOfTable(String table) {
		DBDataUtil dbDataUtil = null;
		String createTblSql = null;
		
		try {
			dbDataUtil = _dataService.getDbManager().createNewDBDataUtil();
			
			createTblSql = dbDataUtil.getSqliteUtil().executeStringScalar(
					"select sql from sqlite_master where lower(tbl_name) = lower('".concat(table).concat("')"));
		} catch (SqliteUtilException e) {
			Log.e("SqlService", "loadColTypesOfTable()", e);
			return;
		} finally {
			try{
				dbDataUtil.close();
			} catch(Exception e) {
			}
		}
		
		if(createTblSql == null || createTblSql.length() == 0) {
			Log.e("SqlService", "loadColTypesOfTable() Warning: Table %@ does not exists in sqlite DB");
			return;
		}
		
		int index0 = createTblSql.indexOf('(');
		int index1 = createTblSql.lastIndexOf(')');
		
		String colsPart = createTblSql.substring(index0 + 1, index1);
		StringTokenizer stk = new StringTokenizer(colsPart, ",");
		
		String colPart = null;
		String colName = null;
		String colType = null;
		int index = 0;
		
		while(stk.hasMoreTokens()) {
			colPart = stk.nextToken().trim();
			
			index = colPart.indexOf(' ');
			if(index > 0) {
				colName = colPart.substring(0, index).trim();
				colType = colPart.substring(index + 1).trim();
				
				setColTypeOfTable(table, colName, colType);
			}
		}
	}
}
