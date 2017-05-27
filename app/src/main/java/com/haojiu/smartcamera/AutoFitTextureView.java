package com.haojiu.smartcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 自定义TextureView来显示预览取景
 */
public class AutoFitTextureView extends TextureView {
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置TextureView要显示的宽高比
     * @param width  宽
     * @param height  高
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        //当view确定自身已经不再适合现有的区域时，该view本身调用requestLayout()方法要求parent view重新调用他的
        //onMeasure onLayout来设置自己位置。
        //特别的,当view的layoutparameter发生改变，并且它的值还没能应用到view上，这时候适合调用这个方法。
        requestLayout();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);//获得尺寸
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);//设置实际大小
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
