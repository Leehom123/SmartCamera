package com.haojiu.smartcamera;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil implements Runnable {

    private Context context;
    private final Image mImage;//要保存的图片数据
    private final File mFile;//保存到的文件

    public FileUtil(Context context, Image image, File file) {
        this.context = context;
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {
        Log.e("FileUtil", "thread name:" + Thread.currentThread().getName());
        BufferedOutputStream bos = null;
        try {

            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);//读取数据到bytes中

            bos = new BufferedOutputStream(new FileOutputStream(mFile));
            bos.write(bytes);//将 b.length 个字节从指定 bytes 数组写入此文件输出流中
            bos.flush();
        } catch (IOException e) {
            Toast.makeText(context, R.string.save_failed, Toast.LENGTH_SHORT).show();
        } finally {
            mImage.close();
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFileName(boolean isPicture) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String datetime =  formatter.format(new Date(System.currentTimeMillis()));
        //若是图片
        if (isPicture) {
            return "IMG_" + datetime + ".jpg";
        } else {
            //若是视频
            return "VID_" + datetime + ".mp4";
        }
    }
}
