package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Range;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Created by leehom on 2017/6/1.
 */

public class VideoSettingActivity extends Activity {

    private ListView lv_fps;
    private int length;
    private ArrayList<Integer> arrayFps;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.layout_video_setting);
        ActivityCollector.addActivity(this);
        setView();
    }
    @Override
    protected void onUserLeaveHint() {
        ActivityCollector.finishAll();
        super.onUserLeaveHint();
    }
    private void setView() {
        TextView title_text_title = (TextView)findViewById(R.id.title_text_title);
        title_text_title.setText("视频设置");
        LinearLayout title_view_main = (LinearLayout)findViewById(R.id.title_view_main);
        title_view_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_fps = (ListView)findViewById(R.id.lv_fps);
        ArrayList<Integer> fps = getIntent().getIntegerArrayListExtra("FPS");
        ArrayList<Integer> fbl = getIntent().getIntegerArrayListExtra("fbl");
        length = getIntent().getIntExtra("length",0);
        for (int i = 0; i< length; i++){
            Integer integer1 = fps.get(i);
            Log.e("zx",integer1+"");
        }
        lv_fps.setAdapter(new MyAdaptor(fps,getApplicationContext(),length,fbl));
        lv_fps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                TextView tv_fps = (TextView)view.findViewById(R.id.tv_fps);
                intent.putExtra("FBL_FPS",tv_fps.getText());
                setResult(0, intent);
                finish();
            }
        });
    }
    private class MyAdaptor extends BaseAdapter{
        private ArrayList<Integer> fps;
        private LayoutInflater mInflater;
        private int length;

        private HashSet<Integer> lvFpsSet;
        private ArrayList<Integer> fbl;
        private Integer fbl1;

        public MyAdaptor(ArrayList<Integer> fps,Context context,int length,ArrayList<Integer> fbl) {
            this.fps = fps;
            this.fbl=fbl;
            this.mInflater = LayoutInflater.from(context);
            this.length=length;
            lvFpsSet = new HashSet<>();
            for (int i=0;i<length;i++){
                Integer integer = fps.get(i);
                lvFpsSet.add(integer);
            }

            Iterator<Integer> iterator = lvFpsSet.iterator();
            arrayFps = new ArrayList<>();
            while (iterator.hasNext()){
                Integer next = iterator.next();
                arrayFps.add(next);
            }

            Collections.sort(arrayFps, new Comparator<Integer>() {

                @Override
                public int compare(Integer o1, Integer o2) {
                    if (o1 < o2)
                        return -1;
                    else if (o1 > o2)
                        return 1;
                    else
                        return o1.compareTo(o2);
                }
            });
        }

        @Override
        public int getCount() {
            return arrayFps.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_video_setting_item, null);
                holder = new ViewHolder();
                holder.tv_fps = (TextView)convertView.findViewById(R.id.tv_fps);
                convertView.setTag(holder); //绑定ViewHolder对象
            } else {
                holder = (ViewHolder) convertView.getTag(); //取出ViewHolder对象
            }
            Integer fps = arrayFps.get(position);
            if (fbl.size()<=position){
                fbl1 = fbl.get(fbl.size()-1);
            }else {
                fbl1 = fbl.get(position);
            }


            holder.tv_fps.setText(fbl1+"P"+fps+"FPS");

            return convertView;
        }
    }
    /*存放控件 的ViewHolder*/
    public final class ViewHolder {
        private TextView tv_fps;
    }
}
