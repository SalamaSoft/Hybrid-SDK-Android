package com.salama.android.datacore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.tools.codec.Base64FormatException;

import com.salama.android.util.SSLog;

import MetoXML.Base.XmlContentEncoder;
import MetoXML.Cast.BaseTypesMapping;
import MetoXML.Util.PropertyDescriptor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SqliteUtil {
	/**
	 * Sqlite数据类型
	 * @author liuxinggu
	 *
	 */
	public enum SqliteColType {SQLITE_TEXT, SQLITE_FLOAT, SQLITE_INTEGER};
	
	private SQLiteDatabase _db;
	private String _dbFilePath;

	// private static SimpleDateFormat JavaUtilDateFormatForParse = new
	// SimpleDateFormat(
	// "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
	// private static SimpleDateFormat JavaSqlTimeStampFormatForParse = new
	// SimpleDateFormat(
	// "yyyy-MM-dd HH:mm:ss.SSS");
	// private static SimpleDateFormat JavaSqlDateFormatForParse = new
	// SimpleDateFormat(
	// "yyyy-mm-dd");

	/**
	 * 编码用于SQL文中的值
	 * <BR>仅将 ' 转换为 ''
	 * @param strValue 字符串
	 * @return 编码后的字符串
	 */
	public static String encodeQuoteChar(String strValue) {
		if (strValue == null || strValue.length() == 0) {
			return strValue;
		} else {
			return strValue.replaceAll("'", "''");
		}
	}
	
	/**
	 * 根据属性类型取得SQLite数据类型
	 * @param propertyType 属性类型
	 * @return Sqlite数据类型
	 */
	public static SqliteColType getSQLiteColumnTypeByPropertyType(Class<?> propertyType) {
		if (propertyType == boolean.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == byte.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == short.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == int.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == long.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == float.class) {
			return SqliteColType.SQLITE_FLOAT;
		} else if (propertyType == double.class) {
			return SqliteColType.SQLITE_FLOAT;
		} else if (propertyType == char.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == Boolean.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == Byte.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == Short.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == Integer.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == Long.class) {
			return SqliteColType.SQLITE_INTEGER;
		} else if (propertyType == Float.class) {
			return SqliteColType.SQLITE_FLOAT;
		} else if (propertyType == Double.class) {
			return SqliteColType.SQLITE_FLOAT;
		} else if (propertyType == Character.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == java.util.Date.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == java.sql.Date.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == java.sql.Timestamp.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == BigDecimal.class) {
			return SqliteColType.SQLITE_TEXT;
		} else if (propertyType == byte[].class) {
			return SqliteColType.SQLITE_TEXT;
		} else {
			return SqliteColType.SQLITE_TEXT;
		}
	}
	
	/**
	 * 转换基础类型(byte,short,int,long,double,byte[],char,java.sql.date,java.util.date)为字符串
	 * @param obj
	 * @return 字符串
	 */
	public static String convertObjectToString(Object obj) {
		if(obj == null) {
			return "";
		} else {
			if (obj.getClass() == byte[].class) {
				try {
					return BaseTypesMapping.EncodeBase64((byte[])obj);
				} catch (IOException e) {
					Log.e("SqliteUtil", "Error occurred in fetchColumnToData() in doing DecodeBase64", e);
					return "";
				}
			} else {
				return BaseTypesMapping.ConvertBaseTypeValueToStr(obj.getClass(), obj);
			}
		}
	}

	/**
	 * 取得SQLiteDataBase
	 * @return SQLiteDataBase
	 */
	public SQLiteDatabase getDb() {
		return _db;
	}

	/**
	 * 构造函数
	 * @param dbFilePath 数据库文件路径
	 */
	public SqliteUtil(String dbFilePath) {
		this._dbFilePath = dbFilePath;
	}

	/**
	 * 打开数据库连接
	 */
	public void open() {
		_db = SQLiteDatabase.openOrCreateDatabase(this._dbFilePath, null);
	}
	
	/**
	 * 关闭数据库连接
	 */
	public void close() {
		if (_db != null) {
			_db.close();
		}
	}

	/**
	 * 查询整型字段
	 * @param sql SQL文
	 * @return 第1条记录的第1个字段
	 */
	public int executeIntScalar(String sql) {
		Cursor cur = null;
		int rtValue = 0;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "executeIntScalar() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			if (cur.moveToNext()) {
				rtValue = cur.getInt(0);
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}

		return rtValue;
	}

	/**
	 * 查询长整型字段
	 * @param sql SQL文
	 * @return 第1条记录的第1个字段
	 */
	public long executeLongScalar(String sql) {
		Cursor cur = null;
		long rtValue = 0;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "executeLongScalar() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			if (cur.moveToNext()) {
				rtValue = cur.getLong(0);
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}

		return rtValue;
	}

	/**
	 * 查询双精度浮点字段
	 * @param sql SQL文
	 * @return 第1条记录的第1个字段
	 */
	public double executeDoubleScalar(String sql) {
		Cursor cur = null;
		double rtValue = 0;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "executeDoubleScalar() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);
			if (cur.moveToNext()) {
				rtValue = cur.getDouble(0);
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}

		return rtValue;
	}

	/**
	 * 查询字符串字段
	 * @param sql SQL文
	 * @return 第1条记录的第1个字段
	 */
	public String executeStringScalar(String sql) {
		Cursor cur = null;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "executeStringScalar() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			if (cur.moveToNext()) {
				return cur.getString(0);
			} else {
				return "";
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
			return "";
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}
	}

	/**
	 * 查询数据
	 * @param sql SQL文
	 * @param dataType 数据类型
	 * @return 数据列表
	 * @throws SqliteUtilException
	 */
	public List<?> findDataList(String sql, Class<?> dataType)
			throws SqliteUtilException {
		List<Object> returnList = new ArrayList<Object>();
		Cursor cur = null;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "findDataList() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			boolean isBaseObjectType = isSupportedBaseObjectType(dataType); 

			//SSLog.d("SqliteUtil", "findDataList() isBaseObjectType:".concat(String.valueOf(isBaseObjectType)));
			
			if(isBaseObjectType) {
				while (cur.moveToNext()) {

					returnList.add(fetchRowForBaseType(cur, dataType));
				}
			} else {
				while (cur.moveToNext()) {

					returnList.add(fetchRowForDataType(cur, dataType));
				}
			}
		} catch (SqliteUtilException e) {
			throw e;
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}

		return returnList;
	}

	private static boolean isSupportedBaseObjectType(Class<?> type) {
		//SSLog.d("SqliteUtil", "isSupportedBaseObjectType() type:" + type + " String.class:" + String.class);
		if(type == String.class) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 查询数据
	 * @param sql SQL文
	 * @param dataType 数据类型
	 * @return 单条数据
	 * @throws SqliteUtilException
	 */
	public Object findData(String sql, Class<?> dataType)
			throws SqliteUtilException {
		Object data = null;
		Cursor cur = null;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "findData() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			if (cur.moveToNext()) {

				data = fetchRowForDataType(cur, dataType);
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}

		return data;
	}

	/**
	 * 查询数据
	 * @param sql SQL文
	 * @param dataType 数据类型
	 * @return 数据列表Xml内容
	 */
	public String findDataListXml(String sql, Class<?> dataType) {
		String dataNodeName = dataType.getSimpleName();
		
		return findDataListXml(sql, dataNodeName);
	}

	/**
	 * 查询数据
	 * @param sql
	 * @param dataNodeName
	 * @return 数据列表Xml内容
	 */
	public String findDataListXml(String sql, String dataNodeName) {
		StringBuilder xmlResult = new StringBuilder();
		Cursor cur = null;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "findDataListXml() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			appendXmlTagBegin(xmlResult, "List");

			while (cur.moveToNext()) {
				appendXmlTagBegin(xmlResult, dataNodeName);

				for (int i = 0; i < cur.getColumnCount(); i++) {
					appendXmlLeafTag(xmlResult,
							cur.getColumnName(i), cur.getString(i));
				}

				appendXmlTagEnd(xmlResult, dataNodeName);
			}

			appendXmlTagEnd(xmlResult, "List");
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}
		return xmlResult.toString();
	}

	/**
	 * 查询数据
	 * @param sql SQL文
	 * @param dataType 数据类型
	 * @return 单条数据Xml内容
	 */
	public String findDataXml(String sql, Class<?> dataType) {
		String dataNodeName = dataType.getSimpleName();
		
		return findDataXml(sql, dataNodeName);
	}

	/**
	 * 查询数据
	 * @param sql SQL文
	 * @param dataType 数据类型
	 * @return 单条数据Xml内容
	 */
	public String findDataXml(String sql, String dataNodeName) {
		StringBuilder xmlResult = new StringBuilder();
		Cursor cur = null;

		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "findDataXml() sql:".concat(sql));
			}
			
			cur = _db.rawQuery(sql, null);

			if (cur.moveToNext()) {
				appendXmlTagBegin(xmlResult, dataNodeName);

				for (int i = 0; i < cur.getColumnCount(); i++) {
					appendXmlLeafTag(xmlResult,
							cur.getColumnName(i), cur.getString(i));
				}

				appendXmlTagEnd(xmlResult, dataNodeName);
			}
		} catch (Exception e) {
			Log.e("SqliteUtil", "", e);
		} finally {
			try {
				cur.close();
			} catch(Exception e) {}
		}
		return xmlResult.toString();
	}
	
	/**
	 * 执行更新语句
	 * @param sql SQL文
	 * @return 1:正常 0:出错
	 */
	public int executeUpdate(String sql) {
		try {
			if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
				SSLog.d("SqliteUtil", "executeUpdate() sql:".concat(sql));
			}
			
			_db.execSQL(sql);
			
			return 1;
		} catch (SQLException e) {
			try {
				if(e.getMessage().endsWith("(code 19)")) {
					SSLog.i("SqliteUtil", e.getMessage() + "\n", e);
					
				} else {
					SSLog.e("SqliteUtil", e.getMessage() + "\n", e);
				}
			} catch(Exception e2) {
				SSLog.e("SqliteUtil", e.getMessage() + "\n", e2);
			}
			return 0;
		}
	}
	
	private Object fetchRowForBaseType(Cursor cur, Class<?> dataType) {
		if(dataType.equals(String.class)) {
			//String val = cur.getString(0);
			//SSLog.d("SqliteUtil", "fetchRowForBaseType() val:" + val);
			return cur.getString(0);
		} else {
			//SSLog.d("SqliteUtil", "fetchRowForBaseType() not supported type");
			return null;
		}
	}

	private Object fetchRowForDataType(Cursor cur, Class<?> dataType)
			throws SqliteUtilException {
		Object data;
		try {
			data = dataType.newInstance();
			for (int i = 0; i < cur.getColumnCount(); i++) {
				fetchColumnToData(dataType, data, cur, i);
			}
		} catch (InstantiationException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalAccessException e) {
			throw new SqliteUtilException(e);
		} catch (IllegalArgumentException e) {
			throw new SqliteUtilException(e);
		} catch (InvocationTargetException e) {
			throw new SqliteUtilException(e);
		} catch (ParseException e) {
			throw new SqliteUtilException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SqliteUtilException(e);
		} catch (IOException e) {
			throw new SqliteUtilException(e);
		} catch (Base64FormatException e) {
			throw new SqliteUtilException(e);
		}
		return data;
	}

	private void fetchColumnToData(Class<?> dataType, Object data, Cursor cur,
			int index) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, ParseException, 
			UnsupportedEncodingException, IOException, Base64FormatException {
		String colName = cur.getColumnName(index);

		PropertyDescriptor pd;
		Method getMethod = null;
		Method setMethod = null;
		try {
			pd = new PropertyDescriptor(colName, dataType);
			getMethod = pd.getReadMethod();

			setMethod = pd.getWriteMethod();
		} catch (NoSuchMethodException e) {
			// do nothing
		}
		if (getMethod == null || setMethod == null) {
			return;
		}

		Class<?> propertyType = (Class<?>) getMethod.getReturnType();

		if (setMethod != null) {
			if (propertyType == boolean.class) {
				setMethod.invoke(data, Boolean.valueOf(cur.getString(index)));
			} else if (propertyType == byte.class) {
				setMethod.invoke(data, Byte.valueOf(cur.getString(index)));
			} else if (propertyType == short.class) {
				setMethod.invoke(data, cur.getShort(index));
			} else if (propertyType == int.class) {
				setMethod.invoke(data, cur.getInt(index));
			} else if (propertyType == long.class) {
				setMethod.invoke(data, cur.getLong(index));
			} else if (propertyType == float.class) {
				setMethod.invoke(data, cur.getFloat(index));
			} else if (propertyType == double.class) {
				setMethod.invoke(data, cur.getDouble(index));
			} else if (propertyType == char.class) {
				setMethod.invoke(data, cur.getString(index).charAt(0));
			} else if (propertyType == Boolean.class) {
				setMethod.invoke(data, Boolean.valueOf(cur.getString(index)));
			} else if (propertyType == Byte.class) {
				setMethod.invoke(data, Byte.valueOf(cur.getString(index)));
			} else if (propertyType == Short.class) {
				setMethod.invoke(data, cur.getShort(index));
			} else if (propertyType == Integer.class) {
				setMethod.invoke(data, cur.getInt(index));
			} else if (propertyType == Long.class) {
				setMethod.invoke(data, cur.getLong(index));
			} else if (propertyType == Float.class) {
				setMethod.invoke(data, cur.getFloat(index));
			} else if (propertyType == Double.class) {
				setMethod.invoke(data, cur.getDouble(index));
			} else if (propertyType == Character.class) {
				setMethod.invoke(data, cur.getString(index).charAt(0));
			} else if (propertyType == java.util.Date.class) {
				setMethod.invoke(data, BaseTypesMapping.ConvertStrToDate(cur.getString(index)));
			} else if (propertyType == java.sql.Date.class) {
				setMethod.invoke(data, BaseTypesMapping.ConvertStrToSqlDate(cur.getString(index)));
			} else if (propertyType == java.sql.Timestamp.class) {
				setMethod.invoke(data, BaseTypesMapping.ConvertStrToTimeStamp(cur.getString(index)));
			} else if (propertyType == BigDecimal.class) {
				setMethod.invoke(data, new BigDecimal(cur.getString(index)));
			} else if (propertyType == byte[].class) {
				setMethod.invoke(data, BaseTypesMapping.DecodeBase64(cur.getString(index)));
			} else {
				setMethod.invoke(data, propertyType.cast(cur.getString(index)));
			}

		}
	}

	// public Method getGetMethod(Class<?> dataType, String colName)
	// {
	// String mName = colName.substring(0, 1).toUpperCase()
	// + colName.substring(1);
	// Method method = null;
	// try {
	// method = dataType.getMethod("get" + mName, (Class<?>[]) null);
	// if(method == null){
	// method = dataType.getMethod("is" + mName, (Class<?>[]) null);
	// }
	// } catch (SecurityException e) {
	// return null;
	// } catch (NoSuchMethodException e) {
	// return null;
	// }
	// return method;
	// }
	//
	// public Method getSetMethod(Class<?> dataType, String colName)
	// {
	// String mName = colName.substring(0, 1).toUpperCase()
	// + colName.substring(1);
	// Method getMethod;
	// Method method;
	// try {
	// getMethod = getGetMethod(dataType, colName);
	// method = dataType.getMethod("set" + mName,
	// getMethod.getReturnType());
	// } catch (SecurityException e) {
	// return null;
	// } catch (NoSuchMethodException e) {
	// return null;
	// }
	// return method;
	// }

	protected static void appendXmlTagBegin(StringBuilder xml, String tagName) {
		xml.append("<");
		xml.append(tagName);
		xml.append(">");
	}

	protected static void appendXmlTagEnd(StringBuilder xml, String tagName) {
		xml.append("</");
		xml.append(tagName);
		xml.append(">");
	}
	
	protected static void appendXmlLeafTag(StringBuilder xml, String tagName, String value) {
		appendXmlTagBegin(xml, tagName);
		
		//encode value
		xml.append(XmlContentEncoder.EncodeContent(value));
		
		appendXmlTagEnd(xml, tagName);
	}
	
}
