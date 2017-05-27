package com.haojiu.smartcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leehom on 2017/5/26.
 */

public class VideoAdapter extends BaseAdapter {
    private List<VideoInfo> list;
    private Context context;
    private LayoutInflater mInflater;

    public VideoAdapter(Context context , List<VideoInfo> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hold = null;
        if (convertView==null) {
            hold = new ViewHolder();
            convertView = mInflater.inflate(R.layout.video_item, null);
            hold.img_suo = (ImageView)convertView.findViewById(R.id.img_suo);
            hold.name = (TextView)convertView.findViewById(R.id.name);
            convertView.setTag(hold);
        }else{
            hold = (ViewHolder) convertView.getTag();
        }
        hold.name.setText(list.get(position).getDisplayName());
        Bitmap bitmap = getVideoThumbnail(list.get(position).getPath(), 240, 240, MediaStore.Images.Thumbnails.MINI_KIND);
        hold.img_suo.setImageBitmap(bitmap);
        return convertView;
    }
    private final class ViewHolder{
        private ImageView img_suo;
        private TextView name;

    }
    /**
     *
     * @param videoPath
     * @param width
     * @param height
     * @param kind
     * @return
     */
    private Bitmap getVideoThumbnail(String videoPath, int width , int height, int kind){
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}
