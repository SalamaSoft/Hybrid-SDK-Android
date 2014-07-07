package com.salama.android.webviewutil;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

public class TitleBarSetting {
	//0xFF3C4E66
	
	/**
	 * 缺省字体大小(20)
	 */
	public static final int DEFAULT_TEXT_SIZE = 20;
	
	/**
	 * 缺省标题栏高度(88)
	 */
	public static final int DEFAULT_TITLE_BAR_HEIGHT = 88;
	
	/**
	 * 缺省背景色(0xFF3C4E66)
	 */
	public static final int DEFAULT_BACKGROUND_T_COLOR = 0xFF3C4E66;
	
//	public static final int DEFAULT_BACKGROUND_T_COLOR_R = 60;
//	public static final int DEFAULT_BACKGROUND_T_COLOR_G = 78;
//	public static final int DEFAULT_BACKGROUND_T_COLOR_B = 102;
//	public static final int DEFAULT_BACKGROUND_T_COLOR_A = 255;
	
	private boolean _hidden = false;
	
	private int _height = DEFAULT_TITLE_BAR_HEIGHT;
	
	private float _titleTextSize = DEFAULT_TEXT_SIZE;
	private int _titleTextAppearanceResId = 0;
	private int _titleTextColor = Color.WHITE;

	//default:60, 78, 102, 255
	private int _backgroundTColor = DEFAULT_BACKGROUND_T_COLOR;
//	private int _backgroundTColorR = DEFAULT_BACKGROUND_T_COLOR_R;
//	private int _backgroundTColorG = DEFAULT_BACKGROUND_T_COLOR_G;
//	private int _backgroundTColorB = DEFAULT_BACKGROUND_T_COLOR_B;
//	private int _backgroundTColorA = DEFAULT_BACKGROUND_T_COLOR_A;

	private int _leftViewBackgroundColor = DEFAULT_BACKGROUND_T_COLOR;
	private int _rightViewBackgroundColor = DEFAULT_BACKGROUND_T_COLOR;
	
	private int _leftViewTextColor = Color.BLACK;
	private int _rightViewTextColor = Color.BLACK;
	
	private String _leftViewTitle = "";
	private String _centerViewTitle = "";
	private String _rightViewTitle = "";

	private Drawable _leftViewBackgroundDrawable = null;
	private Drawable _centerViewBackgroundDrawable = null;
	private Drawable _rightViewBackgroundDrawable = null;

	private Drawable _leftViewIconDrawable = null;
	private Drawable _rightViewIconDrawable = null;
	
	private boolean _leftViewVisible = false;
	private boolean _centerViewVisible = true;
	private boolean _rightViewVisible = false;
	
	private int _leftViewWidth = RelativeLayout.LayoutParams.WRAP_CONTENT;
	private int _centerViewWidth = RelativeLayout.LayoutParams.WRAP_CONTENT;
	private int _rightViewWidth = RelativeLayout.LayoutParams.WRAP_CONTENT;

	private int _leftViewHeight = 68;
	private int _centerViewHeight = RelativeLayout.LayoutParams.WRAP_CONTENT;
	private int _rightViewHeight = 68;
		
	
	/**
	 * 是否隐藏标题栏
	 * @return true:是 false:否
	 */
	public boolean isHidden() {
		return _hidden;
	}

	public void setHidden(boolean hidden) {
		_hidden = hidden;
	}

	/**
	 * 取得左侧标题
	 * @return
	 */
	public String getLeftViewTitle() {
		return _leftViewTitle;
	}
	
	/**
	 * 设置左侧标题
	 * @param leftViewTitle 左侧标题
	 */
	public void setLeftViewTitle(String leftViewTitle) {
		_leftViewTitle = leftViewTitle;
	}
	
	/**
	 * 取得中间标题
	 * @return 中间标题
	 */
	public String getCenterViewTitle() {
		return _centerViewTitle;
	}
	
	/**
	 * 设置中间标题
	 * @param centerViewTitle 中间标题
	 */
	public void setCenterViewTitle(String centerViewTitle) {
		_centerViewTitle = centerViewTitle;
	}
	
	/**
	 * 取得右侧标题
	 * @return 右侧标题
	 */
	public String getRightViewTitle() {
		return _rightViewTitle;
	}
	
	/**
	 * 设置右侧标题
	 * @param rightViewTitle 右侧标题
	 */
	public void setRightViewTitle(String rightViewTitle) {
		_rightViewTitle = rightViewTitle;
	}
	
	/**
	 * 取得左侧背景图
	 * @return 左侧背景图
	 */
	public Drawable getLeftViewBackgroundDrawable() {
		return _leftViewBackgroundDrawable;
	}
	
	/**
	 * 设置左侧背景图
	 * @param leftViewBackgroundDrawable 左侧背景图
	 */
	public void setLeftViewBackgroundDrawable(Drawable leftViewBackgroundDrawable) {
		_leftViewBackgroundDrawable = leftViewBackgroundDrawable;
	}	
	
	/**
	 * 取得中间背景图
	 * @return 中间背景图
	 */
	public Drawable getCenterViewBackgroundDrawable() {
		return _centerViewBackgroundDrawable;
	}
	
	/**
	 * 设置中间背景图
	 * @param centerViewBackgroundDrawable 中间背景图
	 */
	public void setCenterViewBackgroundDrawable(
			Drawable centerViewBackgroundDrawable) {
		_centerViewBackgroundDrawable = centerViewBackgroundDrawable;
	}
	
	/**
	 * 取得右侧背景图
	 * @return 右侧背景图
	 */
	public Drawable getRightViewBackgroundDrawable() {
		return _rightViewBackgroundDrawable;
	}
	
	/**
	 * 设置右侧背景图
	 * @param rightViewBackgroundDrawable 右侧背景图
	 */
	public void setRightViewBackgroundDrawable(Drawable rightViewBackgroundDrawable) {
		_rightViewBackgroundDrawable = rightViewBackgroundDrawable;
	}
	
	/**
	 * 取得是否显示左侧控件
	 * @return 是否显示左侧控件
	 */
	public boolean isLeftViewVisible() {
		return _leftViewVisible;
	}
	
	/**
	 * 设置是否显示左侧控件
	 * @param leftViewVisible 是否显示左侧控件
	 */
	public void setLeftViewVisible(boolean leftViewVisible) {
		_leftViewVisible = leftViewVisible;
	}
	
	/**
	 * 取得是否显示中间控件
	 * @return 是否显示中间控件
	 */
	public boolean isCenterViewVisible() {
		return _centerViewVisible;
	}
	
	/**
	 * 设置是否显示中间控件
	 * @param centerViewVisible 是否显示中间控件
	 */
	public void setCenterViewVisible(boolean centerViewVisible) {
		_centerViewVisible = centerViewVisible;
	}
	
	/**
	 * 取得是否显示右侧控件
	 * @return 是否显示右侧控件
	 */
	public boolean isRightViewVisible() {
		return _rightViewVisible;
	}
	
	/**
	 * 设置是否显示右侧控件
	 * @param rightViewVisible 是否显示右侧控件
	 */
	public void setRightViewVisible(boolean rightViewVisible) {
		_rightViewVisible = rightViewVisible;
	}
	
	/**
	 * 取得左侧标题Id
	 * @return
	 */
	public int getTitleTextAppearanceResId() {
		return _titleTextAppearanceResId;
	}
	
	/**
	 * 设置左侧标题Id
	 * @param titleTextAppearanceResId 左侧标题Id
	 */
	public void setTitleTextAppearanceResId(int titleTextAppearanceResId) {
		_titleTextAppearanceResId = titleTextAppearanceResId;
	}
	
	/**
	 * 取得高度
	 * @return 高度
	 */
	public int getHeight() {
		return _height;
	}
	
	/**
	 * 设置高度
	 * @param height 高度
	 */
	public void setHeight(int height) {
		_height = height;
	}

	/**
	 * 取得标题字体大小
	 * @return 标题字体大小
	 */
	public float getTitleTextSize() {
		return _titleTextSize;
	}
	
	/**
	 * 设置标题字体大小
	 * @param titleTextSize 标题字体大小
	 */
	public void setTitleTextSize(float titleTextSize) {
		_titleTextSize = titleTextSize;
	}
	
	/**
	 * 取得左侧控件宽度
	 * @return 左侧控件宽度
	 */
	public int getLeftViewWidth() {
		return _leftViewWidth;
	}
	
	/**
	 * 设置左侧控件宽度
	 * @param leftViewWidth 左侧控件宽度
	 */
	public void setLeftViewWidth(int leftViewWidth) {
		_leftViewWidth = leftViewWidth;
	}
	
	/**
	 * 取得中间控件宽度
	 * @return 中间控件宽度
	 */
	public int getCenterViewWidth() {
		return _centerViewWidth;
	}
	
	/**
	 * 设置中间控件宽度
	 * @param centerViewWidth 中间控件宽度
	 */
	public void setCenterViewWidth(int centerViewWidth) {
		_centerViewWidth = centerViewWidth;
	}
	
	/**
	 * 取得右侧控件宽度
	 * @return 右侧控件宽度
	 */
	public int getRightViewWidth() {
		return _rightViewWidth;
	}
	
	/**
	 * 设置右侧控件宽度
	 * @param rightViewWidth 右侧控件宽度
	 */
	public void setRightViewWidth(int rightViewWidth) {
		_rightViewWidth = rightViewWidth;
	}
	
	/**
	 * 取得左侧控件高度
	 * @return 左侧控件高度
	 */
	public int getLeftViewHeight() {
		return _leftViewHeight;
	}
	
	/**
	 * 设置左侧控件高度 
	 * @param leftViewHeight 左侧控件高度
	 */
	public void setLeftViewHeight(int leftViewHeight) {
		_leftViewHeight = leftViewHeight;
	}
	
	/**
	 * 取得中间控件高度
	 * @return 中间控件高度
	 */
	public int getCenterViewHeight() {
		return _centerViewHeight;
	}
	
	/**
	 * 设置中间控件高度
	 * @param centerViewHeight 中间控件高度
	 */
	public void setCenterViewHeight(int centerViewHeight) {
		_centerViewHeight = centerViewHeight;
	}
	
	/**
	 * 取得右侧控件高度
	 * @return 右侧控件高度
	 */
	public int getRightViewHeight() {
		return _rightViewHeight;
	}
	
	/**
	 * 设置右侧控件高度
	 * @param rightViewHeight 右侧控件高度
	 */
	public void setRightViewHeight(int rightViewHeight) {
		_rightViewHeight = rightViewHeight;
	}
	
	/**
	 * 取得背景色
	 * @return 背景色
	 */
	public int getBackgroundTColor() {
		return _backgroundTColor;
	}
	
	/**
	 * 设置背景色
	 * @param backgroundTColor 背景色
	 */
	public void setBackgroundTColor(int backgroundTColor) {
		_backgroundTColor = backgroundTColor;
	}
	
	/**
	 * 取得左侧背景色
	 * @return 左侧背景色
	 */
	public int getLeftViewBackgroundColor() {
		return _leftViewBackgroundColor;
	}
	
	/**
	 * 设置左侧背景色
	 * @param leftViewBackgroundColor 左侧背景色
	 */
	public void setLeftViewBackgroundColor(int leftViewBackgroundColor) {
		_leftViewBackgroundColor = leftViewBackgroundColor;
	}
	
	/**
	 * 取得右侧背景色
	 * @return 右侧背景色
	 */
	public int getRightViewBackgroundColor() {
		return _rightViewBackgroundColor;
	}
	
	/**
	 * 设置右侧背景色
	 * @param rightViewBackgroundColor 右侧背景色
	 */
	public void setRightViewBackgroundColor(int rightViewBackgroundColor) {
		_rightViewBackgroundColor = rightViewBackgroundColor;
	}
	
	/**
	 * 取得左侧图标
	 * @return 左侧图标
	 */
	public Drawable getLeftViewIconDrawable() {
		return _leftViewIconDrawable;
	}
	
	/**
	 * 设置左侧图标
	 * @param leftViewIconDrawable 左侧图标
	 */
	public void setLeftViewIconDrawable(Drawable leftViewIconDrawable) {
		_leftViewIconDrawable = leftViewIconDrawable;
	}
	
	/**
	 * 取得右侧图标
	 * @return 右侧图标
	 */
	public Drawable getRightViewIconDrawable() {
		return _rightViewIconDrawable;
	}
	
	/**
	 * 设置右侧图标
	 * @param rightViewIconDrawable 右侧图标
	 */
	public void setRightViewIconDrawable(Drawable rightViewIconDrawable) {
		_rightViewIconDrawable = rightViewIconDrawable;
	}
	
	/**
	 * 取得标题字体颜色
	 * @return 标题字体颜色
	 */
	public int getTitleTextColor() {
		return _titleTextColor;
	}
	
	/**
	 * 设置标题字体颜色
	 * @param titleTextColor 标题字体颜色
	 */
	public void setTitleTextColor(int titleTextColor) {
		_titleTextColor = titleTextColor;
	}
	
	/**
	 * 取得左侧标题颜色
	 * @return 左侧标题颜色
	 */
	public int getLeftViewTextColor() {
		return _leftViewTextColor;
	}
	
	/**
	 * 设置左侧标题颜色
	 * @param leftViewTextColor 左侧标题颜色
	 */
	public void setLeftViewTextColor(int leftViewTextColor) {
		_leftViewTextColor = leftViewTextColor;
	}
	
	/**
	 * 取得右侧标题颜色
	 * @return 右侧标题颜色
	 */
	public int getRightViewTextColor() {
		return _rightViewTextColor;
	}

	/**
	 * 设置右侧标题颜色
	 * @param rightViewTextColor 右侧标题颜色
	 */
	public void setRightViewTextColor(int rightViewTextColor) {
		_rightViewTextColor = rightViewTextColor;
	}
	
	
}
