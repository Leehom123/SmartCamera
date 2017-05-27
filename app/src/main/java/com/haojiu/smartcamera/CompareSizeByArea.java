package com.haojiu.smartcamera;

import android.util.Size;

import java.util.Comparator;

/**
 * 为Size定义一个比较器Comparator
 */
public class CompareSizeByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
        //强转为long保证不会发生溢出
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
}
