package com.salama.android.webcore;

public interface WebVariableStack {
	/**
	 * 临时变量范围(当前页面)
	 */
	public static final int WebVariableStackScopePage = 0;

	/**
	 * 临时变量范围(本次调用)
	 */
	public static final int WebVariableStackScopeTemp = 1;
	
	/**
	 * 清空返回值临时变量
	 */
	void clearVariablesOfAllScope();
	
	/**
	 * 清空返回值临时变量
	 * @param scope 指定范围
	 */
	void clearVariablesOfScope(int scope);
	
	/**
	 * 设置返回值临时变量
	 * @param value 值
	 * @param name 名称
	 * @param scope 范围
	 */
	void setVariable(Object value, String name, int scope);
	
	/**
	 * 取得临时变量值
	 * @param name 名称
	 * @param scope 范围
	 * @return 临时变量值
	 */
	Object getVariable(String name, int scope);
	
	/**
	 * 删除临时变量 
	 * @param name 名称
	 * @param scope 范围
	 */
	void removeVariable(String name, int scope);
}
