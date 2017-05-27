package com.haojiu.smartcamera;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leehom on 2017/5/26.
 */

public class GetVideoInfoUtils {
    private Context mContext;

    // 获取当前目录下所有的mp4文件
    public static List<VideoInfo> GetVideoFileName(String fileAbsolutePath) {
        List<VideoInfo> list = new ArrayList<VideoInfo>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        int index=0;
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                String path = subFile[iFileLength].getPath();
                // 判断是否为MP4结尾

                if (filename.trim().toLowerCase().endsWith(".mp4")) {
                    VideoInfo videoInfo=new VideoInfo(filename,path);
                    list.add(index,videoInfo);
                    index++;
                }
            }
        }
        return list;
    }

    public GetVideoInfoUtils(Context context) {
        this.mContext = context;
    }

}
