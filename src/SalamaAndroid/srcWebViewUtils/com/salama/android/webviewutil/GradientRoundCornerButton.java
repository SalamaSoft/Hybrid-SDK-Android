package com.salama.android.webviewutil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.Button;

/**
 * 圆角，背景渐变的按钮
 *
 */
public class GradientRoundCornerButton extends Button {
	private static final int DEFAULT_BACKGROUND_T_COLOR = 0xFF3C4E66;
	
	/**
	 * 缺省字体大小(14)
	 */
	public static final int DEFAULT_TEXT_SIZE = 14;
	
	/**
	 * 缺省圆角半径(10)
	 */
	public static final int DEFAULT_CORNER_RADIUS = 10;
	
	private int _cornerRadius = DEFAULT_CORNER_RADIUS;

	private Drawable[] _layers = new Drawable[2];
	
	/**
	 * 取得圆角半径
	 * @return 圆角半径
	 */
	public int getCornerRadius() {
		return _cornerRadius;
	}

	/**
	 * 设置圆角半径
	 * @param cornerRadius 圆角半径
	 */
	public void setCornerRadius(int cornerRadius) {
		_cornerRadius = cornerRadius;
	}

	/**
	 * 构造函数
	 * @param context 上下文
	 */
	public GradientRoundCornerButton(Context context) {
		super(context);
		
		setTextSize(DEFAULT_TEXT_SIZE);
		setTypeface(null, Typeface.BOLD);
	}

	@Override
	public void setBackgroundColor(int color) {
		setBackgroundDrawable(createBackgroundImg(color));
	}
	
	@Override
	public void setBackgroundDrawable(Drawable background) {
		if(background == null) {
			return;
		}
		
		super.setBackgroundDrawable(background);
		if(_layers != null) {
			_layers[0] = background;
		}
	}
	
	/**
	 * 设置图标
	 * @param icon 图标
	 */
	public void setIcon(Drawable icon) {
		_layers[1] = icon;

		if(_layers[1] != null) {
			if(_layers[0] == null) {
				_layers[0] = createBackgroundImg(DEFAULT_BACKGROUND_T_COLOR);
			}
			
			LayerDrawable d = new LayerDrawable(_layers);
			super.setBackgroundDrawable(d);
		} else {
			if(_layers[0] != null) {
				super.setBackgroundDrawable(_layers[0]);
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private Drawable createBackgroundImg(int tintColor) {
		int bottomColor = tintColor;
		int a = (bottomColor >> 24) & 0x000000FF;
		int r = (bottomColor >> 16) & 0x000000FF;
		int g = (bottomColor >> 8) & 0x000000FF;
		int b = (bottomColor) & 0x000000FF;
		
		int topColor = transferBottomColorToTopColor(r, g, b, a);
		
		GradientDrawable roundImg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
				new int[]{bottomColor, topColor});
		
		//roundImg.setBounds(left, top, width, height);
		roundImg.setCornerRadius(_cornerRadius);
		return roundImg;
	}
	
	private static int transferBottomColorToTopColor(int r, int g, int b, int a) {
		int delta = 110;
		
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
	
	private static int toIntColor(int r, int g, int b, int a) {
		return ((a << 24) & 0xFF000000) 
				| ((r << 16) & 0x00FF0000)
				| ((g << 8) & 0x0000FF00)
				| (b & 0x000000FF);
	}
	
}
