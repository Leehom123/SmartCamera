package com.haojiu.smartcamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by leehom on 2017/5/22.
 */

public class BanSeekBar extends android.support.v7.widget.AppCompatSeekBar {

    /**
     * 存放拖块的矩形
     */
    private Rect mRect;
    /**
     * 记录当前进度值
     */
    private int mProgressValue;
    /**
     * 禁止点击的时候有进度效果
     */
    private boolean isBanClick=false;
    /**
     * 禁止拖动的时候yon进度效果
     */
    private boolean isBanDrag=false;

    public BanSeekBar(Context context) {
        super(context);
    }

    public BanSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setListener();
    }

    public BanSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setListener();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mProgressValue = getProgress();
        float x = ev.getX();
        float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isBanClick) {
                    return banAction(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isBanDrag) {
                    return banAction(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;

            default:
                break;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * 核心代码，禁止所以动作，不能点击，不能拖动
     * @param x
     * @param y
     * @return
     */
    private boolean banAction(float x, float y) {
        if (mRect != null) {
            if (mRect.contains((int) (x), (int) (y))) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 禁止点击的时候有进度效果
     * @param isBanClick true为禁止，
     */
    public void banClick(boolean isBanClick) {
        this.isBanClick=isBanClick;
    }

    /**
     * 获取点击禁止状态
     */
    public boolean getBanClick() {
        return isBanClick;
    }

    /**
     * 禁止拖动的时候有进度效果
     * @param isBanDrag true为禁止，
     */
    public void banDrag(boolean isBanDrag) {
        this.isBanDrag=isBanDrag;
    }

    /**
     * 获取拖动禁止状态
     */
    public boolean getBanDrag() {
        return isBanDrag;
    }

    /**
     * 设置监听器
     */
    private void setListener() {
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBanClick||isBanDrag) {
                    seekBar.setProgress(mProgressValue);
                }
                if (mBanSeekBarChangeListener!=null) {
                    mBanSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mBanSeekBarChangeListener!=null) {
                    mBanSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            @SuppressLint("NewApi")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (isBanClick||isBanDrag) {
                    // 得到拖块图片
                    Drawable drawable = seekBar.getThumb();
                    // 得到放拖快图片的矩形。
                    mRect = drawable.getBounds();
                }
                if (mBanSeekBarChangeListener!=null) {
                    mBanSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }
        });
    }

    private OnBanSeekBarChangeListener mBanSeekBarChangeListener;

    /**
     * 设置监听
     */
    public void setOnBanSeekBarChangeListener(OnBanSeekBarChangeListener banSeekBarChangeListener) {
        mBanSeekBarChangeListener=banSeekBarChangeListener;
    }



    public interface OnBanSeekBarChangeListener{
        /**
         *
         * @param seekBar
         */
        void onStopTrackingTouch(SeekBar seekBar);

        /**
         *
         * @param seekBar
         */
        void onStartTrackingTouch(SeekBar seekBar);
        /**
         *
         * @param seekBar
         * @param progress
         * @param fromUser
         */
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
    }


}
