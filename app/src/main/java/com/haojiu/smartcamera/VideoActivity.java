package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

import static com.haojiu.smartcamera.GetVideoInfoUtils.GetVideoFileName;

public class VideoActivity extends Activity implements View.OnClickListener {
    private GridView gridview;
    VideoAdapter mAdapter;
    List<VideoInfo> listVideos;
    private DisplayImageOptions mOptions;
    private LinearLayout title_view_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);

        TextView title_text_title = (TextView)findViewById(R.id.title_text_title);
        title_text_title.setText("录像");

        ImageView title_back_btn = (ImageView)findViewById(R.id.title_back_btn);
        title_text_title.setOnClickListener(this);
        title_back_btn.setOnClickListener(this);
        gridview = (GridView) findViewById(R.id.gridview);
        listVideos = GetVideoFileName("/storage/emulated/0/SmartCamera/");
        initImageLoader();
        mAdapter = new VideoAdapter(this, listVideos);
        gridview.setAdapter(mAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(VideoActivity.this, JieVideoPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videoInfo", listVideos.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back_btn:
                finish();
                break;
            case R.id.title_text_title:
                finish();
                break;
        }
    }

    public class LoadedImage {
        Bitmap mBitmap;

        public LoadedImage(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }


}
