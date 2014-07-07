package com.salama.android.webviewutil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.salama.android.support.ServiceSupportApplication;

@SuppressLint("ViewConstructor")
public class TitleBar extends RelativeLayout {
	
	private View _leftView = null;
	private View _centerView = null;
	private View _rightView = null;
	
	private TextView _titleTextView = null;
	private Button _leftButton = null;
	private Button _rightButton = null;

	/*
	public View getLeftView() {
		return _leftView;
	}

	public View getCenterView() {
		return _centerView;
	}

	public View getRightView() {
		return _rightView;
	}
	*/
	
	/**
	 * 取得标题TextView
	 * @return 标题TextView
	 */
	public TextView getTitleTextView() {
		return _titleTextView;
	}

	/**
	 * 设置标题TextView
	 * @param titleTextView 标题TextView
	 */
	public void setTitleTextView(TextView titleTextView) {
		_titleTextView = titleTextView;
	}

	/**
	 * 取得左侧按钮
	 * @return 左侧按钮
	 */
	public Button getLeftButton() {
		return _leftButton;
	}

	/**
	 * 设置左侧按钮
	 * @param leftButton 左侧按钮
	 */
	public void setLeftButton(Button leftButton) {
		_leftButton = leftButton;
	}

	/**
	 * 取得右侧按钮
	 * @return 右侧按钮
	 */
	public Button getRightButton() {
		return _rightButton;
	}

	/**
	 * 设置右侧按钮
	 * @param rightButton 右侧按钮
	 */
	public void setRightButton(Button rightButton) {
		_rightButton = rightButton;
	}
	
	/**
	 * 构造函数
	 * @param context 上下文
	 */
	public TitleBar(Context context) {
		super(context);

		RelativeLayout.LayoutParams titleBarLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, TitleBarSetting.DEFAULT_TITLE_BAR_HEIGHT);
		titleBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		this.setLayoutParams(titleBarLayoutParams);

		//default background color
//		GradientDrawable backDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
//				new int[]{0xFF5A7499, 0xFFBECBDC});
//		this.setBackgroundDrawable(backDrawable);
		setBackGroundTInt255Color(60, 78, 102, 255);
		
		_leftView = createLeftView(context);
		if(_leftView != null) {
			this.addView(_leftView);
		}

		_centerView = createCenterView(context);
		if(_centerView != null) {
			this.addView(_centerView);
			
//			if(_leftView != null) {
//				((RelativeLayout.LayoutParams) _leftView.getLayoutParams()).addRule(
//						RelativeLayout.LEFT_OF, _centerView.getId());
//			}
		}
		
		_rightView = createRightView(context);
		if(_rightView != null) {
//			if(_centerView != null) {
//				((RelativeLayout.LayoutParams) _rightView.getLayoutParams()).addRule(
//						RelativeLayout.RIGHT_OF, _centerView.getId());
//			}
			this.addView(_rightView);
		}
	}

	/**
	 * 装载设置
	 * @param context 上下文
	 * @param setting 设置
	 */
	public void loadSetting(Context context, TitleBarSetting setting) {
		getLayoutParams().height = setting.getHeight();
		
		//TIntColor
		setBackGroundTIntColor(setting.getBackgroundTColor());
		
		if(_titleTextView != null) {
			_titleTextView.setText(setting.getCenterViewTitle());
			_titleTextView.setTextSize(setting.getTitleTextSize());
			
			if(setting.getTitleTextAppearanceResId() != 0) {
				_titleTextView.setTextAppearance(context, setting.getTitleTextAppearanceResId());
			}

			if(setting.isCenterViewVisible()) {
				_titleTextView.setVisibility(View.VISIBLE);
				_titleTextView.setText(setting.getCenterViewTitle());
				_titleTextView.setBackgroundDrawable(setting.getCenterViewBackgroundDrawable());
			} else {
				_titleTextView.setVisibility(View.INVISIBLE);
			}
			
			//_titleTextView.setWidth(setting.getCenterViewWidth());
			//_titleTextView.setHeight(setting.getCenterViewHeight());
		} 
		
		if(_leftButton != null) {
			if(setting.isLeftViewVisible()) {
				_leftButton.setVisibility(View.VISIBLE);
				_leftButton.setText(setting.getLeftViewTitle());
				((GradientRoundCornerButton)_leftButton).setBackgroundColor(
						setting.getLeftViewBackgroundColor());
				if(setting.getLeftViewBackgroundDrawable() != null) {
					_leftButton.setBackgroundDrawable(setting.getLeftViewBackgroundDrawable());
				}
				((GradientRoundCornerButton)_leftButton).setIcon(setting.getLeftViewIconDrawable());
			} else {
				_leftButton.setVisibility(View.INVISIBLE);
			}

//			_leftButton.setWidth(setting.getLeftViewWidth());
//			_leftButton.setHeight(setting.getLeftViewHeight());
		} 
		
		if(_rightButton != null) {
			if(setting.isRightViewVisible()) {
				_rightButton.setVisibility(View.VISIBLE);
				_rightButton.setText(setting.getRightViewTitle());
				((GradientRoundCornerButton)_rightButton).setBackgroundColor(setting.getRightViewBackgroundColor());
				if(setting.getRightViewBackgroundDrawable() != null) {
					_rightButton.setBackgroundDrawable(setting.getRightViewBackgroundDrawable());
				}
				((GradientRoundCornerButton)_rightButton).setIcon(setting.getRightViewIconDrawable());
			} else {
				_rightButton.setVisibility(View.INVISIBLE);
			}

//			_rightButton.setWidth(setting.getRightViewWidth());
//			_rightButton.setHeight(setting.getRightViewHeight());
		} 
		
		if(_leftView != null) {
			_leftView.getLayoutParams().width = setting.getLeftViewWidth();
			_leftView.getLayoutParams().height = setting.getLeftViewHeight();
		}
		
		if(_rightView != null) {
			_rightView.getLayoutParams().width = setting.getRightViewWidth();
			_rightView.getLayoutParams().height = setting.getRightViewHeight();
		}
		
		if(_centerView != null) {
			_centerView.getLayoutParams().width = setting.getCenterViewWidth();
			_centerView.getLayoutParams().height = setting.getCenterViewHeight();
		}
	}
	
	protected View createLeftView(Context context) {
		/*
		RelativeLayout layout = new RelativeLayout(context);
		layout.setId(ServiceSupportApplication.singleton().newViewId());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT);
		layout.setLayoutParams(layoutParams);
		layout.setGravity(RelativeLayout.ALIGN_LEFT | RelativeLayout.CENTER_VERTICAL);
		*/

		_leftButton = new GradientRoundCornerButton(context);
		_leftButton.setId(ServiceSupportApplication.singleton().newViewId());
		_leftButton.setVisibility(INVISIBLE);
		_leftButton.setTextColor(Color.WHITE);
		//_leftButton.setTextSize(16);
		((GradientRoundCornerButton)_leftButton).setBackgroundColor(TitleBarSetting.DEFAULT_BACKGROUND_T_COLOR);
		

		RelativeLayout.LayoutParams buttonLayoutParam = 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		buttonLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		buttonLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
		buttonLayoutParam.setMargins(4, 0, 4, 0);
		_leftButton.setLayoutParams(buttonLayoutParam);
		
//		_leftButton.setGravity(RelativeLayout.ALIGN_LEFT | RelativeLayout.CENTER_VERTICAL);
//		layout.addView(_leftButton);
//		return layout;
		
		return _leftButton;
	}
	
	protected View createCenterView(Context context) {
		//TitleTextView
//		RelativeLayout layout = new RelativeLayout(context);
//		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//		layout.setLayoutParams(layoutParams);
		
		_titleTextView = new TextView(context);
		_titleTextView.setId(ServiceSupportApplication.singleton().newViewId());
		
		_titleTextView.setTextSize(TitleBarSetting.DEFAULT_TEXT_SIZE);
		_titleTextView.setTypeface(null, Typeface.BOLD);
		_titleTextView.setTextColor(Color.WHITE);
		
		RelativeLayout.LayoutParams titleLayoutParam = 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		titleLayoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
		titleLayoutParam.setMargins(4, 0, 4, 0);
		_titleTextView.setLayoutParams(titleLayoutParam);
		
//		_titleTextView.setGravity(RelativeLayout.CENTER_IN_PARENT);
//		layout.addView(_titleTextView);
//		return layout;
		
		return _titleTextView;
	}

	protected View createRightView(Context context) {
		/*
		RelativeLayout layout = new RelativeLayout(context);
		layout.setId(ServiceSupportApplication.singleton().newViewId());
		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_RIGHT);
		layout.setLayoutParams(layoutParams);
		layout.setGravity(RelativeLayout.ALIGN_RIGHT | RelativeLayout.CENTER_VERTICAL);
		*/

		_rightButton = new GradientRoundCornerButton(context);
		_rightButton.setId(ServiceSupportApplication.singleton().newViewId());
		_rightButton.setVisibility(INVISIBLE);
		//_rightButton.setTextSize(16);
		_rightButton.setTextColor(Color.WHITE);
		((GradientRoundCornerButton)_rightButton).setBackgroundColor(TitleBarSetting.DEFAULT_BACKGROUND_T_COLOR);

		RelativeLayout.LayoutParams buttonLayoutParam = 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT 
						);
		buttonLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		buttonLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
		buttonLayoutParam.setMargins(4, 0, 4, 0);
		_rightButton.setLayoutParams(buttonLayoutParam);

//		_rightButton.setGravity(RelativeLayout.ALIGN_RIGHT | RelativeLayout.CENTER_VERTICAL);
//		layout.addView(_rightButton);
//		return layout;
		
		return _rightButton;
	}
	
	public void setBackGroundTFloat1Color(float red, float green, float blue, float alpha) {
		int iRed = (int)(red * 255);
		int iGreen = (int)(green * 255);
		int iBlue = (int)(blue * 255);
		int iAlpha = (int)(alpha * 255);
		
		this.setBackGroundTInt255Color(iRed, iGreen, iBlue, iAlpha);
	}
	
	public void setBackGroundTIntColor(int color) {
		int a = (color >> 24) & 0x000000FF;
		int r = (color >> 16) & 0x000000FF;
		int g = (color >> 8) & 0x000000FF;
		int b = (color) & 0x000000FF;

		setBackGroundTInt255Color(r, g, b, a);
	}
	
	public void setBackGroundTInt255Color(int r, int g, int b, int a) {
		int bottomColor = toIntColor(r, g, b, a);
		int topColor = transferBottomColorToTopColor(r, g, b, a);
		
		GradientDrawable backDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
				new int[]{bottomColor, topColor});
		this.setBackgroundDrawable(backDrawable);
	}
		
	public void setTitle(String title) {
		if(_titleTextView != null) {
			_titleTextView.setText(title);
		}
	}
		
	public static int transferBottomColorToTopColor(int r, int g, int b, int a) {
		/*
		int r2 = (int)(r * 2.1);
		if(r2 > 255) {
			r2 = 255;
		}
		
		int g2 = (int)(g * 1.75);
		if(g2 > 255) {
			g2 = 255;
		}
		
		int b2 = (int)(b * 1.4);
		if(b2 > 255) {
			b2 = 255;
		}
		*/
		
		/*
		int highColor = 220;
		
		int delta = 0;
		if(r >= g && r >= b) {
			delta = highColor - r;
		} else if (g >= r && g >= b) {
			delta = highColor - g;
		} else {
			delta = highColor - b;
		}
		*/
		
		int delta = 150;
		
		if((r + delta) > 255) {
			delta = 255 - r;
		}
		if((g + delta) > 255) {
			delta = 255 - g;
		}
		if((b + delta) > 255) {
			delta = 255 - b;
		}
		
		return toIntColor(r + delta, g + delta, b + delta, a);
	}
	
	public static int toIntColor(int r, int g, int b, int a) {
		return ((a << 24) & 0xFF000000) 
				| ((r << 16) & 0x00FF0000)
				| ((g << 8) & 0x0000FF00)
				| (b & 0x000000FF);
	}
	
	public static int toIntColor(float r, float g, float b, float a) {
		int iR = (int)(r * 255);
		int iG = (int)(g * 255);
		int iB = (int)(b * 255);
		int iA= (int)(a * 255);

		return ((iA << 24) & 0xFF000000) 
				| ((iR << 16) & 0x00FF0000)
				| ((iG << 8) & 0x0000FF00)
				| (iB & 0x000000FF);
	}
	
}
