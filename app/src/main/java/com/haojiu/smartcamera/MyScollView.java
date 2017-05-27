package com.haojiu.smartcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScollView extends ScrollView {

	public MyScollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyScollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MyScollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		onGetDis.getDis(t);
	}

	private OnGetDis onGetDis;

	public void setOnGetDis(OnGetDis onGetDis) {
		this.onGetDis = onGetDis;
	}

	public interface OnGetDis {
		void getDis(int dis);
	}

}
