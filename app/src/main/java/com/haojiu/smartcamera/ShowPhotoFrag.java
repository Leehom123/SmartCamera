package com.haojiu.smartcamera;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leehom on 17-4-19.
 */
public class ShowPhotoFrag extends Activity implements View.OnClickListener{
    private List<String> mList;
    private int mIndex;
    private DisplayImageOptions mOptions;
    private ImageView title_back_btn;
    private TextView title_text_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.show_photo_frag);
        mIndex = getIntent().getIntExtra("index", 0);
        title_text_title = (TextView) findViewById(R.id.title_text_title);
        title_text_title.setText("图片预览");
        title_back_btn = (ImageView)findViewById(R.id.title_back_btn);
        title_text_title.setOnClickListener(this);
        title_back_btn.setOnClickListener(this);
        mList = getPhotosPaths();
        initImageLoader();
        ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.setPageTransformer(true, new MyPageTransformer());
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setCurrentItem(mIndex);
    }
    private void initImageLoader() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.timg)
                .showImageOnFail(R.drawable.timg)
                .showStubImage(R.drawable.timg)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private List<String> getPhotosPaths() {
        List<String> list = new ArrayList<String>();
        File dir = new File(Environment.getExternalStorageDirectory() + "/SmartCamera/");
        File[] files = dir.listFiles();
        for (File file : files) {
            list.add("file:///" + Environment.getExternalStorageDirectory() + "/SmartCamera/" + file.getName());
        }
        return list;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_text_title:
                finish();
                break;
            case R.id.title_back_btn:
                finish();
                break;
        }
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setMaxWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setMaxHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(ShowPhotoFrag.this));
            imageLoader.displayImage(mList.get(position), imageView);
            ((ViewPager) container).addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    class MyPageTransformer implements ViewPager.PageTransformer {
        private float MIN_SCALE = 0.85f;

        private float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                view.setAlpha(0);
            }
        }
    }


}
