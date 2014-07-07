package com.salama.android.support;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import android.content.Context;
import android.util.Log;

import com.salama.android.util.SSLog;

public class UserDefaults {
	public final static String NAME_STANDARD_USER_DEFAULTS = "standard";
	private final static String FILE_NAME_DELIM = ".";
	private final static String FILE_NAME_PREFIX = "salama.userdefaults.";
	
	private static UserDefaults _standardUserDefaults;
	
	private String _name;
	
	public static UserDefaults standardUserDefaults() {
		if(_standardUserDefaults == null) {
			_standardUserDefaults = new UserDefaults(NAME_STANDARD_USER_DEFAULTS);
		}
		
		return _standardUserDefaults;
	}
	
	public UserDefaults(String name) {
		_name = name;
	}

	public Object objectForKey(String key, Class<?> objType) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fis = ServiceSupportApplication.singleton().openFileInput(fileName);
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			XmlDeserializer xmlDes = new XmlDeserializer();
			return xmlDes.Deserialize(reader, objType, 
					ServiceSupportApplication.singleton());
		} catch(FileNotFoundException e) {
			SSLog.d("UserDefaults", "File does not exist:" + fileName);
			return null;
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in read file input:" + fileName, e);
			return null;
		} finally {
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
	}
	
	public void setObject(Object obj, Class<?> objType, String key) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fos = ServiceSupportApplication.singleton().openFileOutput(fileName, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);

			if(obj != null) {
				XmlSerializer xmlSer = new XmlSerializer();
				xmlSer.Serialize(writer, obj, objType);
			}
			
			writer.flush();
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in write file ouput:" + fileName, e);
		} finally {
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
	}
	
	public String stringForKey(String key) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fis = ServiceSupportApplication.singleton().openFileInput(fileName);
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			StringBuilder sb = new StringBuilder();

			char[] chrBuff = new char[128];
			int readCnt;
			while(true) {
				readCnt = reader.read(chrBuff, 0, chrBuff.length);
				
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt != 0) {
					sb.append(chrBuff, 0, readCnt);
				}
			}
			
			return sb.toString();
		} catch(FileNotFoundException e) {
			SSLog.d("UserDefaults", "File does not exist:" + fileName);
			return null;
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in read file input:" + fileName, e);
			return null;
		} finally {
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
	}
	
	public void setString(String value, String key) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fos = ServiceSupportApplication.singleton().openFileOutput(fileName, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);

			writer.write(value);
			
			writer.flush();
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in write file ouput:" + fileName, e);
		} finally {
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
	}
	
	public boolean boolForKey(String key) {
		String boolStr = stringForKey(key);
		if(boolStr != null && boolStr.equals("1")) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setBool(boolean value, String key) {
		if(value) {
			setString("1", key);
		} else {
			setString("0", key);
		}
	}
	
	public byte[] dataForKey(String key) {
		FileInputStream fis = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fis = ServiceSupportApplication.singleton().openFileInput(fileName);

			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			
			byte[] byteBuff = new byte[1024];
			int readCnt;
			while(true) {
				readCnt = fis.read(byteBuff, 0, byteBuff.length);
				
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt != 0) {
					byteOutput.write(byteBuff, 0, byteBuff.length);
				}
			}
			
			return byteOutput.toByteArray();
		} catch(FileNotFoundException e) {
			SSLog.d("UserDefaults", "File does not exist:" + fileName);
			return null;
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in read file input:" + fileName, e);
			return null;
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
		}
	}
	
	public void setData(byte[] value, String key) {
		FileOutputStream fos = null;
		
		String fileName = fileNameOfKey(key);
		try {
			fos = ServiceSupportApplication.singleton().openFileOutput(fileName, Context.MODE_PRIVATE);

			fos.write(value);
			
			fos.flush();
		} catch(Exception e) {
			Log.e("UserDefaults", "Error in write file ouput:" + fileName, e);
		} finally {
			try {
				fos.close();
			} catch(Exception e) {
			}
		}
	}

	public float floatForKey(String key) {
		String valStr = stringForKey(key);
		
		if(valStr == null) {
			return 0;
		} else {
			return Float.parseFloat(valStr);
		}
	}
	
	public void setFloat(float value, String key) {
		setString(Float.toString(value), key);
	}
	
	public int integerForKey(String key) {
		String valStr = stringForKey(key);
		
		if(valStr == null) {
			return 0;
		} else {
			return Integer.parseInt(valStr);
		}
	}
	
	public void setInteger(int value, String key) {
		setString(Integer.toString(value), key);
	}
	
	public double doubleForKey(String key) {
		String valStr = stringForKey(key);
		
		if(valStr == null) {
			return 0;
		} else {
			return Double.parseDouble(valStr);
		}
	}
	
	public void setDouble(double value, String key) {
		setString(Double.toString(value), key);
	}
	
	private String fileNameOfKey(String key) {
		return FILE_NAME_PREFIX + _name + FILE_NAME_DELIM + key;
	}
}
