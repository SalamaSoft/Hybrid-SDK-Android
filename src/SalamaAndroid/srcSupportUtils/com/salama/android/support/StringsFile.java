package com.salama.android.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class StringsFile {
	public static final Charset DefaultCharset = Charset.forName("UTF-8");
	public static final String StringsFileNameExtention = ".strings";
	
	private HashMap<String, String> _stringValueMapping = new HashMap<String, String>();
	
	
	public StringsFile(InputStream inputStream) throws IOException {
		reload(inputStream);
	}

	/**
	 * 重新读取装载
	 * @param inputStream
	 * @throws IOException
	 */
	public void reload(InputStream inputStream) throws IOException {
		_stringValueMapping.clear();
		
		load(inputStream);
	}
	
	/**
	 * 取得内容
	 * @param key
	 * @return 内容
	 */
	public String getValue(String key) {
		return _stringValueMapping.get(key);
	}
	
	/**
	 * Content is like the format below:
	 *  "tabBar.product.title" = "产品";
	 *  "tabBar.search.title" = "搜索";
	 *  
	 *  This is the format of .strings in IOS.
	 * @param inputStream
	 * @throws IOException
	 */
	private void load(InputStream inputStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inputStream, DefaultCharset);
		BufferedReader bufReader = null;
		try {
			bufReader = new BufferedReader(reader);
			
			String strLine = null;
			int indexQuote0, indexQuote1, indexEqual, indexQuote2, indexQuote3;
			while(true) {
				strLine = bufReader.readLine();
				
				if(strLine == null) {
					break;
				}
				
				strLine = strLine.trim();
				
				if(strLine.length() == 0 || strLine.charAt(strLine.length() - 1) != ';') {
					continue;
				}
						
				indexQuote0 = strLine.indexOf('"');
				indexQuote1 = strLine.indexOf('"', indexQuote0 + 1);
				indexEqual = strLine.indexOf('=', indexQuote1 + 1);
				indexQuote2 = strLine.indexOf('"', indexEqual + 1);
				indexQuote3 = strLine.lastIndexOf('"');
				
				_stringValueMapping.put(strLine.substring(indexQuote0 + 1, indexQuote1), 
						strLine.substring(indexQuote2 + 1, indexQuote3));
			}
			
		} finally {
			try {
				reader.close();
			} catch(Exception e) {
			}
			try {
				bufReader.close();
			} catch(Exception e) {
			}
		}
	}
}
