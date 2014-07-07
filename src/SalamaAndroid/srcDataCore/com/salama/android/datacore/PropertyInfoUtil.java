package com.salama.android.datacore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import MetoXML.Util.PropertyDescriptor;

public class PropertyInfoUtil {
	
	/**
	 * 取得属性列表
	 * @param dataCls 类型
	 * @return
	 */
	public static List<PropertyDescriptor> getPropertyInfoList(Class<?> dataCls) {
		List<PropertyDescriptor> propertyList = new ArrayList<PropertyDescriptor>();

		String propName = null;
		Method[] methods = dataCls.getMethods();
		String methodName = null;
		
		PropertyDescriptor propertyDesc;
		
		for (int i = 0; i < methods.length; i++) {
			methodName = methods[i].getName();
			
			if(methodName.startsWith("get")) {
				propName = String.valueOf(methodName.charAt(3)).toLowerCase().concat(methodName.substring(4));
				
				try {
					propertyDesc = new PropertyDescriptor(propName, dataCls);
				} catch(NoSuchMethodException e) {
					continue;
				}
				
				if (isPropertyHasReadWriteMethod(propertyDesc)) {
					propertyList.add(propertyDesc);
				}
			}
		}
		
		return propertyList;
	}
	
	/**
	 * 取得属性名列表
	 * @param dataCls 类型
	 * @return
	 */
	public static List<String> getPropertyNameList(Class<?> dataCls) {
		List<String> propertyList = new ArrayList<String>();
		
		String propName = null;
		Method[] methods = dataCls.getMethods();
		String methodName = null;
		
		for (int i = 0; i < methods.length; i++) {
			methodName = methods[i].getName();
			
			if(methodName.startsWith("get")) {
				propName = String.valueOf(methodName.charAt(3)).toLowerCase().concat(methodName.substring(4));
				if (isFieldHasReadWriteMethod(dataCls, propName)) {
					propertyList.add(propName);
				}
			}
		}
		
		return propertyList;
	}
	
	
	/**
	 * 检测属性是否拥有读写方法
	 * @param dataCls 类型
	 * @param fieldName 属性名
	 * @return true:是 false:否
	 */
	public static boolean isFieldHasReadWriteMethod(Class<?> dataCls,
			String fieldName) {
		Method getMethod = null;
		Method setMethod = null;
		PropertyDescriptor propertyDesc;
		
		try {
			propertyDesc = new PropertyDescriptor(fieldName, dataCls);
			getMethod = propertyDesc.getReadMethod();
			setMethod = propertyDesc.getWriteMethod();
			
		} catch (NoSuchMethodException e) {
			return false;
		}

		if (getMethod != null && setMethod != null) {
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * 检测属性是否拥有读写方法
	 * @param propertyDesc 属性
	 * @return true:是 false:否
	 */
	public static boolean isPropertyHasReadWriteMethod(PropertyDescriptor propertyDesc) {
		Method getMethod = null;
		Method setMethod = null;
		
		getMethod = propertyDesc.getReadMethod();
		setMethod = propertyDesc.getWriteMethod();

		if (getMethod != null && setMethod != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	private static String formatFieldName(String fieldName) {
		if(fieldName.charAt(0) == '_') {
			return fieldName.substring(1);
		} else {
			return fieldName;
		}
	}
	*/
	
}
