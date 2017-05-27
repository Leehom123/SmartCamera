package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by leehom on 2017/4/19.
 */

public class UserBackActivity extends Activity implements View.OnClickListener{

    private EditText et_userback;
    private Button btn_commit;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.layout_user_back_activity);
        et_userback = (EditText)findViewById(R.id.et_userback);
        btn_commit = (Button)findViewById(R.id.btn_commit);
        btn_commit.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_commit:
                String et_path = et_userback.getText().toString();
                String d="http://192.168.0.105:8080/smartCamera/CameraSuggestService?requestType=1&suggestInfo=";
                try {
                    String pathEncode = URLEncoder.encode(et_path, "utf-8");
                    path = d+pathEncode;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
              getMethed(path);
                this.finish();
                break;
        }
    }

    private void getMethed(final String path){
        final StringBuffer buffer = new StringBuffer();
        new Thread(){

            private String result;

            public void run() {
                try {
                    URL url=new URL(path);
                    //getConnection()得到HttpURLConnection对象，来发送或接受数据，
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //给服务器发送get请求
                    conn.setRequestMethod("GET");//如果不写这句也是发送get请求
                    //设置请求超时时间
                    conn.setConnectTimeout(4000);
                    //获取服务器响应码
                    int code = conn.getResponseCode();
                    if (code==200) {
                        //获取服务器返回的数据，是一流的形式返回的，流转换成字符串很常用所以抽取成一个util类
                        // 将返回的输入流转换成字符串
                        InputStream inputStream = conn.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        String str = null;
                        while ((str = bufferedReader.readLine()) != null) {
                            buffer.append(str);
                        }
                        result = buffer.toString();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // 更新UI
                                Toast.makeText(getApplicationContext(), result,Toast.LENGTH_LONG).show();
                            }
                        });
                        bufferedReader.close();
                        inputStreamReader.close();
                        // 释放资源
                        inputStream.close();
                        inputStream = null;
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            };

        }.start();


    }
}
