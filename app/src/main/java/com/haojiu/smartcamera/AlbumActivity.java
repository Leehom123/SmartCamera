package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leehom on 2017/4/19.
 */

public class AlbumActivity extends Activity implements View.OnClickListener{

    private List<String> mList;
    private DisplayImageOptions mOptions;
    private ImageView title_back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.layout_album_activity);
        TextView title_text_title =  (TextView)findViewById(R.id.title_text_title);
        title_text_title.setText("相册");
        title_back_btn = (ImageView)findViewById(R.id.title_back_btn);
        title_back_btn.setOnClickListener(this);
        title_text_title.setOnClickListener(this);
        GridView gv = (GridView) findViewById(R.id.gv_album);
        initImageLoader();
        mList = getPhotosPaths();
        gv.setAdapter(new MyGridViewAdapter());
        gv.setOnItemClickListener(new MyGridViewItemClick());
        }

    private List<String> getPhotosPaths() {
        List<String> list = new ArrayList<String>();
        checkParentDir();
        checkJpegDir();
        File dir = new File(Environment.getExternalStorageDirectory() + "/SmartCamera/");
        File[] files = dir.listFiles();
        for (File file : files) {
            list.add("file:///" + Environment.getExternalStorageDirectory() + "/SmartCamera/" + file.getName());
        }
        return list;
    }
    private void checkParentDir() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/SmartCamera/");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private void checkJpegDir() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/SmartCamera/");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private void initImageLoader() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.timg)
                .showStubImage(R.drawable.timg)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_back_btn:
                finish();
                break;
            case R.id.title_text_title:
                finish();
                break;
        }
    }

    class MyGridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.item_grid, null);
                vh.imageView = (ImageView) convertView.findViewById(R.id.item_gv);
                convertView.setTag(vh);
            }
            vh = (ViewHolder) convertView.getTag();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(AlbumActivity.this));
            imageLoader.displayImage(mList.get(position), vh.imageView, mOptions);
            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageView;
    }
    class MyGridViewItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startActivity(new Intent(getApplicationContext(), ShowPhotoFrag.class).putExtra("index",position));
        }
    }
}
