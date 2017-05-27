package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    startActivity(new Intent(SplashActivity.this,CameraActivity.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Intent intent = new Intent(); //调用照相机
//                intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
//                startActivity(intent);
                finish();
            }
        }).start();
    }
}
