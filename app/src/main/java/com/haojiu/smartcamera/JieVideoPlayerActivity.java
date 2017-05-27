package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by leehom on 2017/5/26.
 */

public class JieVideoPlayerActivity extends Activity implements View.OnClickListener {
    private SurfaceView surface;
    private MediaPlayer mediaplay;
    private String urlPath ;
    private Intent intent;
    private int postion = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.play);
        TextView title_text_title = (TextView)findViewById(R.id.title_text_title);
        ImageView title_back_btn = (ImageView)findViewById(R.id.title_back_btn);
        title_text_title.setOnClickListener(this);
        title_back_btn.setOnClickListener(this);
        surface = (SurfaceView) findViewById(R.id.surface);
        mediaplay = new MediaPlayer();
        surface.getHolder().setKeepScreenOn(true); //设置全屏
        surface.getHolder().addCallback(new SurfaceViewLis());
        intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        VideoInfo video = (VideoInfo) bundle.getSerializable("videoInfo");
        urlPath = video.getPath();
        String displayName = video.getDisplayName();
        title_text_title.setText(displayName);
    }
    public void play() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        mediaplay.reset();
        mediaplay.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaplay.setDataSource(urlPath);
        // 把视频输出到SurfaceView上
        mediaplay.setDisplay(surface.getHolder());
        mediaplay.prepare();
        mediaplay.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_text_title:
                finish();
                break;
            case R.id.title_back_btn:
                finish();
                break;
        }
    }

    private class SurfaceViewLis implements SurfaceHolder.Callback{
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // TODO Auto-generated method stub
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (postion == 0) {
                try {
                    play();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }}