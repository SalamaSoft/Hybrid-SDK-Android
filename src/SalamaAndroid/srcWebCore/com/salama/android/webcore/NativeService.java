package com.salama.android.webcore;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import MetoXML.XmlDeserializer;
import MetoXML.Base.XmlParseException;
import MetoXML.Cast.BaseTypesMapping;
import MetoXML.Util.PropertyDescriptor;
import android.util.Log;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;

public class NativeService {
	protected static final String JS_PREFIX_NATIVE_SERVICE = "nativeService://";
	protected static final String JS_PREFIX_NATIVE_SERVICE_LOWER = "nativeservice://";
	protected static final String SPECIAL_SERVICE_THIS_VIEW = "thisView";
	
	
	private HashMap<String, Object> _targetDict = new HashMap<String, Object>();
	
	/**
	 * 解析指令
	 * @param cmd 指令内容
	 * @return InvokeMsg或List<InvokeMsg>
	 */
	public static Object parseNativeServiceCmd(String cmd) {
		if(cmd.startsWith(JS_PREFIX_NATIVE_SERVICE) 
				|| cmd.startsWith(JS_PREFIX_NATIVE_SERVICE_LOWER)) {
			return InvokeMsg.invokeMsgWithXml(InvokeMsg.decodeURLString(cmd.substring(16)));
		} else {
			return null;
		}
	}
	
	/**
	 * 注册service
	 * @param serviceName service名称
	 * @param service service实例
	 */
	public void registerService(String serviceName, Object service) {
		_targetDict.put(serviceName, service);
	} 
	
	/**
	 * 调用
	 * @param targetName 目标对象
	 * @param methodName 方法名
	 * @param params 参数列表
	 * @param thisView thisView
	 * @return 调用返回值
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws XmlParseException
	 * @throws IOException
	 * @throws ParseException
	 */
	public Object invoke(String targetName, String methodName, 
			List<String> params, Object thisView) throws InvocationTargetException,
			NoSuchMethodException, IllegalAccessException, InstantiationException,
			XmlParseException, IOException, ParseException {
		return invokeImp(targetName, methodName, params, thisView);
	}

	private Object invokeImp(String targetName, String methodName, 
			List<String> params, Object thisView) throws InvocationTargetException,
			NoSuchMethodException, IllegalAccessException, InstantiationException,
			XmlParseException, IOException, ParseException {
		Object target = null;
		
		if(SPECIAL_SERVICE_THIS_VIEW.equals(targetName)) {
			target = thisView;
		} else {
			target = findTarget(targetName, thisView);
		}
		
	    int paramsCount = 0;
	    if(params != null) {
	    	paramsCount = params.size();
	    }
	    
	    if(target == null) {
	    	SSLog.e("NativeService", "invokeImp() Not found target:" + targetName);
	    	return "";
	    } else {
	    	SSLog.d("NativeService", "invokeImp() target:" + targetName + " targetType:" + target.getClass().getSimpleName());
	    }

    	Method method = findMethod(target, methodName, paramsCount);
	    
	    if(method == null) {
	    	Log.e("NativeService", "invoke() Not found method:" + methodName);
	    	return "";
	    }
	    
	    Class<?>[] paramTypes = method.getParameterTypes();
	    Object[] paramValues = new Object[paramTypes.length];
	    Class<?> paramType;
	    String paramXml;
	    String param;
	    String xmlTagName;
	    int indexFirstTag;
	    int index2FirstTag;
	    
	    for(int i = 0; i < paramTypes.length; i++) {
	    	paramType = paramTypes[i];
	    	paramXml = params.get(i);
	    	
	    	SSLog.d("NativeService()", "paramType[" + i + "]:" + paramType + " paramXml:" + paramXml);
	    	
	    	if(paramXml.length() == 0) {
	    		paramValues[i] = null;
	    		continue;
	    	}
	    	
	    	indexFirstTag = paramXml.indexOf('<');
	    	index2FirstTag = paramXml.indexOf('>', indexFirstTag);
	    	xmlTagName = paramXml.substring(indexFirstTag + 1, index2FirstTag);
	    	
	    	if(xmlTagName.equals("String")) {
	    		param = (String) XmlDeserializer.stringToObject(paramXml, String.class, 
	    				ServiceSupportApplication.singleton());
	    		
	    		if(param.startsWith("$$")) {
                    //decode: "$$" -> "$"
	    			paramValues[i] = param.substring(1);
	    		} else if (param.startsWith("$")) {
                    //value stack
	    			paramValues[i] = findValueFromWebVariableStack(
	    					param.substring(1), (WebVariableStack)thisView);
	    		} else {
	    			paramValues[i] = BaseTypesMapping.Convert(paramType, param);
	    		}
	    	} else if (xmlTagName.equals("Object")) {
	    		paramValues[i] = XmlDeserializer.stringToObject(
	    				paramXml, paramType, ServiceSupportApplication.singleton());
	    	} else if (xmlTagName.equals("List")) {
	    		paramValues[i] = XmlDeserializer.stringToObject(
	    				paramXml, ArrayList.class, ServiceSupportApplication.singleton());
	    	} else if (xmlTagName.equals("double")) {
	    		param = (String) XmlDeserializer.stringToObject(paramXml, String.class, 
	    				ServiceSupportApplication.singleton());
	    		paramValues[i] = BaseTypesMapping.Convert(paramType, param);
	    	} else {
	    		paramValues[i] = XmlDeserializer.stringToObject(
	    				paramXml, paramType, ServiceSupportApplication.singleton());
	    	}
	    }
	    
	    //invoke
	    if(method.getReturnType() == void.class) {
	    	method.invoke(target, paramValues);
	    	return null;
	    } else {
		    Object returnVal = method.invoke(target, paramValues);
		    return returnVal;
	    }
	} 
	
	private Method findMethod(Object target, String methodName, int paramsCount) {
		Method[] methods = target.getClass().getMethods();
		Method method;
		
		for(int i = 0; i < methods.length; i++) {
			method = methods[i];
			
			if(method.getName().equals(methodName) 
					&& method.getParameterTypes().length == paramsCount) {
				return method;
			}
		}
		
		return null;
	}
	
	private Object findTarget(String targetName, Object thisView) throws InvocationTargetException,
	NoSuchMethodException, IllegalAccessException {
		int index = targetName.indexOf('.');
		
		Object targetObj = null;
		String targetObjName = null;
		
		if(index < 0) {
			targetObjName = targetName;
		} else {
			targetObjName = targetName.substring(0, index);
		}
		
		if(SPECIAL_SERVICE_THIS_VIEW.equals(targetObjName)) {
			targetObj = thisView;
		} else if (targetObjName.startsWith("$")) {
	        //value stack
			if(isInstanceAssignableToClass(thisView, WebVariableStack.class)) {
				String varName = targetObjName.substring(1);
				
				targetObj = findValueFromWebVariableStack(varName, (WebVariableStack)thisView);
				
				if(targetObj == null) {
					return null;
				}
			} else {
	            //not support variable stack
				return null;
			}
		} else {
	        //dict
			targetObj = _targetDict.get(targetObjName);
			if(targetObj == null) {
				return null;
			}
		}
		
		if(index < 0) {
			return targetObj;
		} else {
			String propertyNameTmp = null;
			
		 	int previousIndex = index;
		 	int indexTmp;
			PropertyDescriptor propDesc = null;
			
			while(true) {
				indexTmp =  targetName.indexOf('.', previousIndex + 1);
				
				if(indexTmp < 0) {
					propertyNameTmp = targetName.substring(previousIndex + 1); 

					propDesc = new PropertyDescriptor(propertyNameTmp, targetObj.getClass());
					targetObj = propDesc.getReadMethod().invoke(targetObj, (Object[])null);
					
					return targetObj;
				} else {
					propertyNameTmp = targetName.substring(previousIndex + 1, indexTmp); 

					propDesc = new PropertyDescriptor(propertyNameTmp, targetObj.getClass());
					targetObj = propDesc.getReadMethod().invoke(targetObj, (Object[])null);
				}
				
				previousIndex = indexTmp;
			}
			
		}
	}
	
	private Object findValueFromWebVariableStack(String varName, WebVariableStack thisView) {
		Object targetObj = null;
		
		targetObj = thisView.getVariable(varName, WebVariableStack.WebVariableStackScopeTemp);
		if(targetObj == null) {
			targetObj = thisView.getVariable(varName, WebVariableStack.WebVariableStackScopePage);
		}
		if(targetObj == null && varName.equals(SPECIAL_SERVICE_THIS_VIEW)) {
			return thisView;
		} else {
			return targetObj;
		}
	}
	
	/**
	 * 检测对象实例是否可转换为指定类型
	 * @param obj 对象实例
	 * @param toClass 类型
	 * @return
	 */
	public static boolean isInstanceAssignableToClass(Object obj, Class<?> toClass) {
		try {
			Object obj2 = toClass.cast(obj);
			
			return (obj2 == obj);
		} catch(Exception e) {
			return false;
		}
	}
	
}
