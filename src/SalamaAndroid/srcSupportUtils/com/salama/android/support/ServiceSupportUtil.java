package com.salama.android.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceSupportUtil {
	
	//HashMap<FileName, StringsFile>
	private static HashMap<String, StringsFile> _stringsFileMapping = new HashMap<String, StringsFile>();
	
	private ServiceSupportUtil() {
		
	}
	
	/**
	 * 装载.strings文件。文件格式为ios代码中的.strings文件。
	 * @param fileName 文件名
	 * @param inputStream 
	 * @throws IOException
	 */
	public static void loadStringsFile(String fileName, InputStream inputStream) throws IOException {
		StringsFile file = new StringsFile(inputStream);
		_stringsFileMapping.put(fileName, file);
	}
	
	/**
	 * 取得.strings文件的内容
	 * @param key 键
	 * @param fileName 文件名
	 * @return key对应的内容
	 */
	public static String getStringsValueByKey(String key, String fileName) {
		return _stringsFileMapping.get(fileName).getValue(key);
	}
	
	/**
	 * 数组转列表
	 * @param strArray 数组
	 * @return 列表
	 */
	public static List<String> newList(String[] strArray) {
		List<String> strList = new ArrayList<String>();
		
		for(int i = 0; i < strArray.length; i++) {
			strList.add(strArray[i]);
		}
		
		return strList;
	}
	
	
}
