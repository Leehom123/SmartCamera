package com.haojiu.smartcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by leehom on 2017/4/6.
 */

public class CameraActivity extends Activity implements SensorEventListener,View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final int WINDOW_TEXT_DISAPPEAR = 101;
    public static final int FOCUS_AGAIN = 102;
    public static final int FOCUS_DISAPPEAR = 100;
    private static final String TAG = "CameraActivity";
    private HandlerThread mBackgroundThread;//定义后台线程
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray PRIVIEW_ORIENTATIONS = new SparseIntArray();
    //Sensor方向，大多数设备是90度
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    //Sensor方向，一些设备是270度
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private ImageButton mButton;
    private Integer mSensorOrientation;//Sensor方向
    private File mFile;//图片的保存位置
    private Handler mBackgroundHandler;//定义后台线程的Handler
    private Boolean mFlashSupported;//是否支持闪光灯
    private CameraCaptureSession mCameraCaptureSession;//定义CameraCaptureSession成员变量
    private ImageReader mImageReader;//用于保存图片的ImageReader对象
    private CaptureRequest.Builder mPreviewRequestBuilder;//预览请求的CaptureRequest.Builder对象
    private CaptureRequest.Builder mCaptureBuilder;//拍照的CaptureRequest.Builder对象
    private Size mPreviewSize;//预览大小
    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;//代表摄像头的成员变量
    private String mCameraId="0";//摄像头ID（通常0代表后置摄像头，1代表前置摄像头）
    private Semaphore mCameraLock = new Semaphore(1);//Camera互斥锁
    private int valueAE, valueISO, sleepTime = 0;
    private Boolean flag1 = true, flag2 = true, flag3 = true;//lag1 与ES/ISO 对应，flag2与WB对应，flag3 与MF对应
    private SeekBar sb_ev;
    private CameraCharacteristics characteristics = null;
    private CameraManager cameraManager;
    private TextView zoom_text;
    private TextView es_text;
    private ImageButton btn_iso;
    private TextView tv_iso;
    private MySeekBarListener mySeekBarListener;
    private ImageButton btn_wb;
    private ImageButton btn_zipai;
    private ImageButton btn_auto;
    private ImageButton btn_style;
    private ImageButton btn_setting;
    private ImageButton btn_luxiang;
    private ImageButton btn_xiangce;
    private ImageButton btn_disp;
    int cTime = 1,cTime_luxiang,cTime_setting,cTime_spx ,cTime_cl, cTime_fd, cTime_splash,cTime_language,cTime_dir, seekTime = 0, seekTime2 = 0;
    private Timer timer, timer_type, timer_wb;
    private TimerTask task, task_type, task_wb;
    private CameraLine camera_line;
    private ImageButton btn_ev;
    private ImageButton btn_zoom;
    private Button matul_btn;
    private Button automatic;
    private ImageButton btn_xiangji;
    private int ae;
    private String mNextVideoAbsolutePath;
    private Size mVideoSize;
    private SensorManager sm;
    private PopupWindow mPopWindow;
    /**
     * focus的图
     */
    private AnimationImageView mFocusImage;
    protected static final int MSG_REFRESH_UI = 1000;//电池电量
    //拍照权限请求码
    private static final int REQUEST_PICTURE_PERMISSION = 1;
    //拍照权限
    private static final String[] PICTURE_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    //    sensor的方向为270度时向为90度时，屏幕方向与Sensor方向的对应关系 90 0 270 180
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //sensor的方向为270度时，屏幕方向与Sensor方向的对应关系 270 180 90 0
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * UI线程的handler
     */
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FOCUS_DISAPPEAR:
                    if (msg.obj == null) {
                        mFocusImage.stopFocus();
                        break;
                    }
                    Integer valueTimes = (Integer) msg.obj;
                    if (mFocusImage.mTimes == valueTimes.intValue()) {
                        mFocusImage.stopFocus();
                    }
                    break;
                case WINDOW_TEXT_DISAPPEAR:
                    if (msg.obj == null) {
                        break;
                    }
                    Integer valueTimes2 = (Integer) msg.obj;

                    break;
                case FOCUS_AGAIN:
                    Log.i("FOCUS_AGAIN", "FOCUS_AGAINFOCUS_AGAINFOCUS_AGAIN");
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                    updatePreview();
                    break;
            }
        }
    };
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera(width, height);//打开相机
            configureTransform(width, height);
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    private RelativeLayout rl_top;
    private RelativeLayout rl_bottom;
    private TextView power_text;
    private TextView romsize_text;
    private Button btn_fankui;
    private RelativeLayout rl_setting;
    private Button type_matul_btn;
    private Button type_automatic;
    private TextView setting_second_state;
    private BanSeekBar sb_zoom;
    private BanSeekBar sb_jizhun;
    private BanSeekBar sb_kuaimen;
    private BanSeekBar sb_buchang_type;
    private MyBanSeekBarListener myBanSeekBarListener;
    private BanSeekBar bansb_sewen;
    private BanSeekBar bansb_sediao;
    private BanSeekBar bansb_iso;
    private MediaRecorder mMediaRecorder;
    private Button lock_btn;
    private TextView tv_type;
    private TextView tv_type_jizhun;
    private TextView tv_type_kuaimen;
    private TextView tv_type_buchang;
    private TextView tv_wb_mode;
    private TextView tv_wb_mode_sewen;
    private TextView tv_wb_mode_sediao;
    private TextView tv_power;
    private Range<Integer>[] exposeFPSRange;
    private ArrayList<Integer> list;
    private int length;
    private ArrayList<Integer> fbl_list;
    private TextView setting_second_state_vedio;
    private MediaPlayer mPlayer;
    private FrameLayout fl_spx;
    private GSensitiveView gSensitiveView;
    private Chronometer my_chronometer;
    private RelativeLayout rl_video_redpoint;
    /**
     * Focus的Scale动画
     */
    private ScaleAnimation mScaleFocusAnimation;
    /**
     * 用来focus的显示框框之类的
     */
    private PreviewSessionCallback mPreviewSessionCallback;
    private CameraCharacteristics cameraCharacteristics;
    private String text_yssy="取消";
    private TextView yssy_text_time;
    private RelativeLayout rl_yssy_text;
    private Timer timer_yssy;
    private TimerTask timerTask_yssy;
    private TextView setting_name_bluetooth;
    private TextView setting_name_video;
    private TextView setting_name_handdirection;
    private TextView setting_name_fangdou;
    private TextView setting_name_splash;
    private TextView setting_name_powertype;
    private TextView setting_name_cameraline;
    private TextView setting_second_state_dir;
    private TextView setting_second_state_power_type;
    private TextView camera_line_text;
    private TextView splash_text;
    private TextView setting_second_state_shuiping;
    private TextView setting_name_shuiping;
    private TextView text_fangdou;
    private TextView setting_second_state_bluetooth;
    private TextView setting_second_state_language;
    private String fileName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        // 显示界面
        setContentView(R.layout.activity_camera);
        startBackgroundThread();
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.kacha);
        /*
        * 手动对焦有关
        * */
        mScaleFocusAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleFocusAnimation.setDuration(200);



        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        zoom_text = (TextView) findViewById(R.id.zoom_text);
        es_text = (TextView) findViewById(R.id.es_text);
        mButton = (ImageButton) findViewById(R.id.picture);
        btn_iso = (ImageButton) findViewById(R.id.iso);


        sb_ev = (SeekBar) findViewById(R.id.sb_ev);
        sb_zoom = (BanSeekBar) findViewById(R.id.sb_zoom);
        btn_wb = (ImageButton) findViewById(R.id.wb);
        btn_auto = (ImageButton) findViewById(R.id.auto);
        btn_style = (ImageButton) findViewById(R.id.style);
        btn_setting = (ImageButton) findViewById(R.id.setting);
        btn_luxiang = (ImageButton) findViewById(R.id.luxiang);
        btn_xiangce = (ImageButton) findViewById(R.id.xiangce);
        btn_zipai = (ImageButton) findViewById(R.id.zipai);
        btn_disp = (ImageButton) findViewById(R.id.disp);

        camera_line = (CameraLine) findViewById(R.id.id_cl);
        btn_ev = (ImageButton) findViewById(R.id.ev);
        btn_zoom = (ImageButton) findViewById(R.id.zoom);


        btn_xiangji = (ImageButton) findViewById(R.id.xiangji);
        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        power_text = (TextView) findViewById(R.id.power_text);
        tv_power = (TextView)findViewById(R.id.tv_power);
        romsize_text = (TextView) findViewById(R.id.romsize_text);
        btn_fankui = (Button) findViewById(R.id.fankui);
        rl_setting = (RelativeLayout) findViewById(R.id.rl_setting);

        setting_second_state = (TextView) findViewById(R.id.setting_second_state);





        fl_spx = (FrameLayout)findViewById(R.id.shuipingxian);
        rl_video_redpoint = (RelativeLayout)findViewById(R.id.rl_video_redpoint);
        my_chronometer = (Chronometer)findViewById(R.id.my_chronometer);
        yssy_text_time = (TextView)findViewById(R.id.yssy_text_time);
        rl_yssy_text = (RelativeLayout)findViewById(R.id.rl_yssy_text);

        my_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
            }
        });

        mFocusImage = (AnimationImageView) findViewById(R.id.img_focus);
        mFocusImage.setVisibility(View.INVISIBLE);
        mFocusImage.setmMainHandler(mMainHandler);
        mFocusImage.setmAnimation(mScaleFocusAnimation);
        initFocusImage();
        sb_ev.setMax(100);
        sb_zoom.setMax(100);
        sb_ev.setProgress(50);
        sb_zoom.setProgress(0);
        valueAE = 0;
        valueISO = 0;
        //button点击事件
        btn_luxiang.setOnClickListener(this);
        mButton.setOnClickListener(this);
        btn_iso.setOnClickListener(this);
        btn_wb.setOnClickListener(this);
        btn_zipai.setOnClickListener(this);
        btn_disp.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
        btn_ev.setOnClickListener(this);
        btn_zoom.setOnClickListener(this);
        btn_auto.setOnClickListener(this);
        btn_fankui.setOnClickListener(this);
        btn_xiangce.setOnClickListener(this);
        btn_style.setOnClickListener(this);
        btn_xiangji.setOnClickListener(this);
        //seekBar监听器
        mySeekBarListener = new MySeekBarListener();
        myBanSeekBarListener = new MyBanSeekBarListener();
        String sdAvailableSize = new Utils(getApplicationContext()).getSDAvailableSize();
        String romTotalSize = new Utils(getApplicationContext()).getRomTotalSize();
        romsize_text.setText(sdAvailableSize + "/" + romTotalSize);
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            mDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
//        }
        //重力水平线对象
        gSensitiveView = new GSensitiveView(getApplicationContext());
        fl_spx.addView(gSensitiveView);

    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                if (!layout_setting_include.isFocusable())
//                layout_setting_include.setVisibility(View.GONE);
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    //获取手机电量
    Handler mhHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
//                    power_text.setText( msg.obj.toString() + "%");
                    break;
            }
        }
    };

    /**
     * 打开相机
     * @param width  SurfaceTexture的宽
     * @param height SurfaceTexture的高
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {

        PermissionUtil permissionUtil = new PermissionUtil(this);
        //若没有权限
        if (!permissionUtil.hasPermissionGranted(PICTURE_PERMISSIONS)) {
            //请求所需权限
            permissionUtil.requestRequiredPermissions(PICTURE_PERMISSIONS, R.string.need_permissions, REQUEST_PICTURE_PERMISSION);
            return;
        }

        if (this.isFinishing()) {
            return;
        }
        setCameraInfo(width, height);//设置Camera信息
        //获得Camera的系统服务管理器
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //若超过2500毫秒，Camera仍未打开
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("相机打开超时");
            }

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
            HashSet<Integer> fbl=new HashSet<>();
            if (outputSizes != null) {
                for (Size sz : outputSizes) {
                    int height1 = sz.getHeight();
                    int width1 = sz.getWidth();
                    if(height1>=720){
                        height1=720;
                    }else if (height1<720&&height1>=480){
                        height1=480;
                    }else if (height1<480){
                        height1=240;
                    }
                    fbl.add(height1);
                    Log.e("zx",width1+"*"+height1);
                }
                fbl_list = new ArrayList<>();
                Iterator<Integer> iterator = fbl.iterator();
                while (iterator.hasNext()){
                    Integer next = iterator.next();
                    fbl_list.add(next);
                }
            }
            //视频FPS值
            exposeFPSRange = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            list = new ArrayList<Integer>();
            length = exposeFPSRange.length;
            for (int i = 0; i< length; i++){
                Range<Integer> integerRange = exposeFPSRange[i];
                Integer upper = integerRange.getUpper();
                Integer lower = integerRange.getLower();
                list.add(upper);
            }
            //打开Camera
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            configureTransform(width, height);
            newPreviewSession();
            mMediaRecorder = new MediaRecorder();
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
        } catch (InterruptedException e) {
            throw new RuntimeException("打开相机时中断");
        } catch (CameraAccessException e) {
            throw new RuntimeException("无法访问相机");
        }
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraLock.release();
            mCameraDevice = cameraDevice;
            startPreview();//开始预览
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraLock.release();
            cameraDevice.close();
            mCameraDevice = null;
//            CameraActivity.this.finish();
        }
    };

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    /**
     * 开始预览
     */
    private void startPreview() {
        if (mCameraDevice == null || !mTextureView.isAvailable() || mPreviewSize == null) {
            return;
        }
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();

        //设置预览大小
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        Surface surface = new Surface(surfaceTexture);

        try {
            //创建作为预览的CaptureRequest.Builder对象
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mPreviewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, PRIVIEW_ORIENTATIONS.get(rotation));

            //将mTextureView的surface作为CaptureRequest.Builder的目标
            mPreviewRequestBuilder.addTarget(surface);
            setBuilder(mPreviewRequestBuilder);
            //创建用于预览和拍照的CameraCaptureSession
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    mCameraCaptureSession = cameraCaptureSession;
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);

                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    //开始显示的时候将触摸变焦监听器设置了
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    mTextureView.setmMyTextureViewTouchEvent(new TextureViewTouchEvent(cameraCharacteristics, mTextureView, mPreviewRequestBuilder, mCameraCaptureSession, mMainHandler, mPreviewSessionCallback));
                    updatePreview();//更新预览
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新预览
     */
    private void updatePreview() {
        //设置CaptureRequest.Builder对象
//        setBuilder(mPreviewRequestBuilder);
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //预览
                    mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreviewSessionCallback, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    throw new RuntimeException("无法访问相机");
                }
            }
        });
    }

    /**
     * 设置CaptureRequest.Builder对象
     * @param captureRequestBuilder  要设置的CaptureRequest.Builder对象
     */
    private void setBuilder(CaptureRequest.Builder captureRequestBuilder) {
        // 设置自动对焦模式
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);

        //若支持闪光灯
        if (mFlashSupported) {
            //设置自动曝光模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        }
    }

    /**
     * 设置Camera信息
     * @param width  SurfaceTexture的宽
     * @param height  SurfaceTexture的高
     */
    private void setCameraInfo(int width, int height) {
        //获得Camera的系统服务管理器
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //通过标识符返回当前连接的相机设备的列表，包括可能在使用其他相机客户端的相机
            String[] cameraIds = cameraManager.getCameraIdList();

                //获得指定CameraId相机设备的属性
                cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);

//                //获得摄像头朝向
//                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
//                //若是前置摄像头，不做任何操作
//                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    return;
//                }
                //获得流配置
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    return;
                }
                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                //获取摄像头支持的最大尺寸
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
                //创建一个ImageReader对象，用于获取摄像头的图像数据。设置图片大小为largest
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);
                int orientation = getResources().getConfiguration().orientation;
                //若是横屏
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(width, height);
                } else {
                    //若是竖屏
                    mTextureView.setAspectRatio(width, height);
                }

                sb_zoom.setOnSeekBarChangeListener(mySeekBarListener);
                sb_ev.setOnSeekBarChangeListener(mySeekBarListener);

                //是否有闪光灯
                Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = (available == null ? false : available);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //获取捕获的照片数据
            Image image = reader.acquireNextImage();
            Log.e("zx", mFile.toString());
            mBackgroundHandler.post(new FileUtil(CameraActivity.this, image, mFile));
        }
    };

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {

        if (null == mTextureView || null == mPreviewSize || null == getApplicationContext()) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation ) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("zx", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    @Override
    public void onClick(View view) {
        SharedPreferences seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
        mFocusImage.stopFocus();
        switch (view.getId()) {
            case R.id.picture:
                showPopupWindow();
                break;
            case R.id.xiangji:
                if (Utils.isFastClick(1)) {
                    return;
                }
                if (text_yssy.equals("取消")){
                    sleepTime=0;
                }else if (text_yssy.equals("3s")){
                    sleepTime=3;
                }else if (text_yssy.equals("6s")){
                    sleepTime=6;
                }else if (text_yssy.equals("10s")){
                    sleepTime=10;
                }
                //yssy的文字显示
                if (timer_yssy==null){
                    timer_yssy = new Timer();
                }
                if (timerTask_yssy==null){
                    timerTask_yssy = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (text_yssy.equals("取消")){
                                        rl_yssy_text.setVisibility(View.GONE);
                                        if (timer_yssy!=null){
                                            timer_yssy.cancel();
                                            timer_yssy=null;
                                        }
                                        if (timerTask_yssy!=null){
                                            timerTask_yssy.cancel();
                                            timerTask_yssy=null;
                                        }
                                    }else {
                                        rl_yssy_text.setVisibility(View.VISIBLE);
                                        sleepTime--;
                                        if (sleepTime<0){
                                            rl_yssy_text.setVisibility(View.GONE);
                                        }else {
                                            yssy_text_time.setText("拍照倒计时："+sleepTime+"s");
                                        }

                                    }
                                }
                            });
                        }
                    };
                }
                timer_yssy.schedule(timerTask_yssy,20,1000);
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(sleepTime * 1000);
                                takePicture();//拍照

                                if (rl_yssy_text.getVisibility()!=View.GONE){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            rl_yssy_text.setVisibility(View.GONE);
                                        }
                                    });
                                }
                                    if (timer_yssy!=null){
                                        timer_yssy.cancel();
                                        timer_yssy=null;
                                    }
                                    if (timerTask_yssy!=null){
                                        timerTask_yssy.cancel();
                                        timerTask_yssy=null;
                                    }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                break;
            case R.id.auto:
                flag1 = true;
                flag2 = true;
                flag3 = true;
                if (bansb_sewen==null || bansb_sediao==null){
                    showWBPopWindow();
                    mPopWindow.dismiss();
                }
                if (sb_jizhun==null || sb_kuaimen==null){
                    showTypePopWindow();
                    mPopWindow.dismiss();
                }
                if (bansb_iso==null){
                    showIsoPopWindow();
                    mPopWindow.dismiss();
                }
                bansb_sewen.banDrag(true);
                bansb_sewen.banClick(true);
                bansb_sediao.banDrag(true);
                bansb_sediao.banClick(true);
                bansb_sewen.setAlpha(0.3f);
                bansb_sediao.setAlpha(0.3f);
                sb_jizhun.banDrag(true);
                sb_jizhun.banClick(true);
                sb_kuaimen.banDrag(true);
                sb_kuaimen.banClick(true);
                bansb_iso.banDrag(true);
                bansb_iso.banClick(true);
                sb_jizhun.setAlpha(0.3f);
                sb_kuaimen.setAlpha(0.3f);
                bansb_iso.setAlpha(0.3f);
                if (flag1 == true && flag2 == true && flag3 == true) {
                    btn_auto.setAlpha(0.3f);
                    timer = new Timer();
                    task = new TimerTask() {

                        private Integer ae;

                        @Override
                        public void run() {
                            Log.e("zx", "timer Auto.......");
//                            ae = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);

                            if (seekTime % 3 == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        sb_ev.setProgress(ae.intValue() * (100 / 24) + 50);
//                                        es_text.setText(ae + "");
                                    }
                                });

                            }
                        }
                    };
                    timer.schedule(task, 500, 500);

                }
                break;
            case R.id.matul_btn://wb 手动按钮
                flag2 = false;
                bansb_sewen.banDrag(false);
                bansb_sewen.banClick(false);
                bansb_sediao.banDrag(false);
                bansb_sediao.banClick(false);
                bansb_sewen.setAlpha(1);
                bansb_sediao.setAlpha(1);
                if (flag1 == false || flag2 == false || flag3 == false) {
                    btn_auto.setAlpha(0.9f);
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (timer_wb != null) {
                        timer_wb.cancel();
                    }
                }
                break;
            case R.id.automatic://wb 自动按钮
                flag2 = true;
                bansb_sewen.banDrag(true);
                bansb_sewen.banClick(true);
                bansb_sediao.banDrag(true);
                bansb_sediao.banClick(true);
                bansb_sewen.setAlpha(0.3f);
                bansb_sediao.setAlpha(0.3f);
                if (flag2 == true && flag3 == true) {
                    btn_auto.setAlpha(0.3f);
                    timer_wb = new Timer();
                    task_wb = new TimerTask() {
                        @Override
                        public void run() {
                            Log.e("zx", "timer.2 WB......");
                        }
                    };
                    timer_wb.schedule(task_wb, 500, 500);
                }
                break;
            case R.id.type_automatic://小太阳 里边的自动按钮
                flag1 = true;
                sb_jizhun.banDrag(true);
                sb_jizhun.banClick(true);
                sb_kuaimen.banDrag(true);
                sb_kuaimen.banClick(true);
                bansb_iso.banDrag(true);
                bansb_iso.banClick(true);
                sb_jizhun.setAlpha(0.3f);
                sb_kuaimen.setAlpha(0.3f);
                bansb_iso.setAlpha(0.3f);
                if (flag1 == true && flag3 == true) {
                    btn_auto.setAlpha(0.3f);
                    timer_type = new Timer();
                    task_type = new TimerTask() {
                        @Override
                        public void run() {
                            Log.e("zx", "timer.2 type......");
                        }
                    };
                    timer_type.schedule(task_type, 500, 500);
                }
                break;
            case R.id.type_matul_btn://小太阳 里边的手动按钮
                flag1 = false;
                sb_jizhun.banDrag(false);
                sb_jizhun.banClick(false);
                sb_kuaimen.banDrag(false);
                sb_kuaimen.banClick(false);
                bansb_iso.banDrag(false);
                bansb_iso.banClick(false);
                sb_jizhun.setAlpha(1);
                sb_kuaimen.setAlpha(1);
                bansb_iso.setAlpha(1);
                if (flag1 == false || flag2 == false || flag3 == false) {
                    btn_auto.setAlpha(0.9f);
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (timer_type != null) {
                        timer_type.cancel();
                    }
                }
                break;
            case R.id.style:
                showTypePopWindow();
                break;
            case R.id.iso:

                showIsoPopWindow();
                break;
            case R.id.wb:
                showWBPopWindow();
                break;
            case R.id.zipai:
                if (Utils.isFastClick(1)) {
                    return;
                }
                if (mCameraId.equals("0")) {
                    mCameraId = "1";
                    closeCamera();
                    changeCamera();
                } else {
                    mCameraId = "0";
                    closeCamera();
                    changeCamera();
                }

                break;
            case R.id.disp:
                if (cTime % 2 == 1) {
                    btn_auto.setVisibility(View.INVISIBLE);
                    btn_style.setVisibility(View.INVISIBLE);
                    btn_wb.setVisibility(View.INVISIBLE);
                    btn_iso.setVisibility(View.INVISIBLE);
                    btn_setting.setVisibility(View.INVISIBLE);
                    btn_zipai.setVisibility(View.INVISIBLE);
                    btn_luxiang.setVisibility(View.INVISIBLE);
                    btn_xiangji.setVisibility(View.INVISIBLE);
                    power_text.setVisibility(View.INVISIBLE);
                    romsize_text.setVisibility(View.INVISIBLE);
                    tv_power.setVisibility(View.INVISIBLE);
                    rl_bottom.setBackground(null);
                    rl_top.setBackground(null);
                } else {
                    btn_auto.setVisibility(View.VISIBLE);
                    btn_style.setVisibility(View.VISIBLE);
                    btn_wb.setVisibility(View.VISIBLE);
                    btn_iso.setVisibility(View.VISIBLE);
                    btn_setting.setVisibility(View.VISIBLE);
                    btn_zipai.setVisibility(View.VISIBLE);
                    btn_luxiang.setVisibility(View.VISIBLE);
                    btn_xiangji.setVisibility(View.VISIBLE);
                    power_text.setVisibility(View.VISIBLE);
                    romsize_text.setVisibility(View.VISIBLE);
                    tv_power.setVisibility(View.VISIBLE);
                    rl_bottom.setBackgroundColor(0xff2E2E2E);
                    rl_top.setBackgroundColor(0xff2E2E2E);
                }
                cTime++;
                break;
            case R.id.setting:
                showSettingPopWindow();
                break;
            case R.id.layout_setting_fangdou_include:
                cTime_fd++;
                if (cTime_fd % 2 == 1) {
                    if (cTime_language%2==1){
                        text_fangdou.setText("opened");
                    }else {
                        text_fangdou.setText("已开启");
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);
                } else {

                    if (cTime_language%2==1){
                        text_fangdou.setText("closed");
                    }else {
                        text_fangdou.setText("已关闭");
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                }
                break;
            case R.id.layout_setting_camerline_include:
                cTime_cl++;
                if (cTime_cl % 2 == 1) {
                    camera_line.setVisibility(View.VISIBLE);
                    if (cTime_language%2==1){
                        camera_line_text.setText("opened");
                    }else {
                        camera_line_text.setText("已开启");
                    }

                } else {
                    camera_line.setVisibility(View.GONE);
                    if (cTime_language%2==1){
                        camera_line_text.setText("closed");
                    }else {
                        camera_line_text.setText("已关闭");
                    }
                }
                break;
            case R.id.layout_setting_shuiping_include:
                cTime_spx++;
                if (cTime_spx%2==1){
                    sm = (SensorManager) getSystemService(SENSOR_SERVICE);
                    sm.registerListener( this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

                    fl_spx.setVisibility(View.VISIBLE);
                    if (cTime_language%2==1){
                        setting_second_state_shuiping.setText("opened");
                    }else {
                        setting_second_state_shuiping.setText("已开启");
                    }
                }else {
                    fl_spx.setVisibility(View.GONE);
                    sm.unregisterListener(this);
                    if (cTime_language%2==1){
                        setting_second_state_shuiping.setText("closed");
                    }else {
                        setting_second_state_shuiping.setText("已关闭");
                    }
                }



                break;
            case R.id.layout_setting_handdirection_include:
                cTime_dir++;
                if (cTime_dir%2==1){
                    if (cTime_language%2==1){
                        setting_second_state_dir.setText("opposite direction");
                    }else {
                        setting_second_state_dir.setText("反向");
                    }
                }else{
                    if (cTime_language%2==1){
                        setting_second_state_dir.setText("forward direction");
                    }else {
                        setting_second_state_dir.setText("正向");
                    }
                }
                break;
            case R.id.layout_setting_vedio_include:
                startActivityForResult(new Intent(CameraActivity.this,VideoSettingActivity.class).putIntegerArrayListExtra("FPS",list)
                .putExtra("length",length).putIntegerArrayListExtra("fbl",fbl_list),0);


                break;
            case R.id.layout_setting_splash_include://闪光灯设置
                cTime_splash++;
//                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                if (cTime_splash % 3 == 1) {//已开启
                    if (cTime_language%2==1){
                        splash_text.setText("opened");
                    }else {
                        splash_text.setText("已开启");
                    }
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);

                } else if (cTime_splash % 3 == 2) {//自动
                    if (cTime_language%2==1){
                        splash_text.setText("auto");
                    }else {
                        splash_text.setText("自动");
                    }
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);

                } else {//已关闭
                    if (cTime_language%2==1){
                        splash_text.setText("closed");
                    }else {
                        splash_text.setText("已关闭");
                    }
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
//                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                }
//                updatePreview();

                break;
            case R.id.ev:
                seekTime++;
                if (seekTime % 3 == 0) {
                    //ev
                    int ev = seekbar_value.getInt("ev", 50);
                    String ev_text = seekbar_value.getString("ev_text", "0");
                    sb_ev.setProgress(ev);
                    es_text.setText(ev_text);
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_ev));
                } else if (seekTime % 3 == 1) {
                    //es
                    int es = seekbar_value.getInt("es", 50);
                    String es_text_value = seekbar_value.getString("es_text", "50");
                    sb_ev.setProgress(es);
                    es_text.setText(es_text_value);
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_es));
                } else {
                    //iso
                    int iso = seekbar_value.getInt("iso", 50);
                    String iso_text = seekbar_value.getString("iso_text", "1750");
                    sb_ev.setProgress(iso);
                    es_text.setText(iso_text);
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_iso));
                }
                break;
            case R.id.zoom:

                seekTime2++;
                if (seekTime2 % 3 == 0) {
                    //ZOOM
                    int zoom = seekbar_value.getInt("zoom", 0);
                    String zoom_text_value = seekbar_value.getString("zoom_text", "0");
                    sb_zoom.setProgress(zoom);
                    zoom_text.setText(zoom_text_value);
                    btn_zoom.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_zoom));
                } else if (seekTime2 % 3 == 1) {
                    //MF
                    int mf = seekbar_value.getInt("mf", 0);
                    String mf_text = seekbar_value.getString("mf_text", "0");
                    sb_zoom.setProgress(mf);
                    zoom_text.setText(mf_text);
                    btn_zoom.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_mf));
                } else {
                    //WB
                    int wb = seekbar_value.getInt("wb", 11);
                    String wb_text = seekbar_value.getString("wb_text", "1");
                    sb_zoom.setProgress(wb);
                    zoom_text.setText(wb_text);
                    btn_zoom.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_wb));
                }
                break;
            case R.id.fankui:
                startActivity(new Intent(CameraActivity.this, UserBackActivity.class));
                break;
            case R.id.xiangce:
                startActivity(new Intent(CameraActivity.this, YuLanActivity.class));
                break;
            case R.id.layout_setting_bluetooth_include:
                startActivity(new Intent(CameraActivity.this, BlueTooth.class));
                break;
            case R.id.luxiang:
                if (Utils.isFastClick(2)) {
                    return;
                }
                cTime_luxiang++;
                if (cTime_luxiang%2==1){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                                0);
                    }else {
                        startRecordingVideo();
                    }
                    rl_video_redpoint.setVisibility(View.VISIBLE);
                    btn_luxiang.setAlpha(0.3f);
                    my_chronometer.setFormat("%s");
                    my_chronometer.setBase(SystemClock.elapsedRealtime());
                    my_chronometer.start();
                    btn_xiangji.setClickable(false);
                    btn_wb.setClickable(false);
                    btn_iso.setClickable(false);
                    btn_style.setClickable(false);
                    btn_zipai.setClickable(false);
                    btn_xiangce.setClickable(false);
                    btn_setting.setClickable(false);
                    mButton.setClickable(false);
                }else {
                    stopRecordingVideo();
                    my_chronometer.stop();
                    btn_luxiang.setAlpha(1.0f);
                    rl_video_redpoint.setVisibility(View.GONE);
                    btn_xiangji.setClickable(true);
                    btn_wb.setClickable(true);
                    btn_iso.setClickable(true);
                    btn_style.setClickable(true);
                    btn_zipai.setClickable(true);
                    btn_xiangce.setClickable(true);
                    btn_setting.setClickable(true);
                    mButton.setClickable(true);
                }

                break;
            case R.id.layout_setting_language_include:
                cTime_language++;
                if (cTime_language%2==1){
                    //电量
                    tv_power.setText("Power：");
                    setting_second_state_language.setText("English");
                    //设置
                    setting_name_bluetooth.setText("BlueTooth");
                    setting_name_video.setText("VideoSetting");
                    setting_name_handdirection.setText("Handwheel Direction");
                    setting_name_fangdou.setText("Anti-shake");
                    setting_name_splash.setText("Splash");
                    setting_name_powertype.setText("Power Type");
                    setting_name_cameraline.setText("Guide");
                    setting_name_shuiping.setText("Gravity horizontal line");
                    if (splash_text.getText().equals("已关闭")){
                        splash_text.setText("closed");
                    }else if (splash_text.getText().equals("自动")){
                        splash_text.setText("auto");
                    }else {
                        splash_text.setText("opened");
                    }
                    if (camera_line_text.getText().equals("已开启")){
                        camera_line_text.setText("opened");
                    }else{
                        camera_line_text.setText("closed");
                    }
                    if (text_fangdou.getText().equals("已开启")){
                        text_fangdou.setText("opened");
                    }else {
                        text_fangdou.setText("closed");
                    }
                    if (setting_second_state_dir.getText().equals("反向")){
                        setting_second_state_dir.setText("opposite direction");
                    }else {
                        setting_second_state_dir.setText("forward direction");
                    }
                    if (setting_second_state_bluetooth.getText().equals("未连接")){
                        setting_second_state_bluetooth.setText("disconnected");
                    }else{
                        setting_second_state_bluetooth.setText("connected");
                    }
                    if (setting_second_state_shuiping.getText().equals("已开启")){
                        setting_second_state_shuiping.setText("opened");
                    }else {
                        setting_second_state_shuiping.setText("closed");
                    }
                }else {
                    //电量
                    tv_power.setText("电量：");
                    setting_second_state_language.setText("中文");
                    //设置
                    setting_name_bluetooth.setText("蓝牙");
                    setting_name_video.setText("视频设置");
                    setting_name_handdirection.setText("手轮方向");
                    setting_name_fangdou.setText("防抖");
                    setting_name_splash.setText("闪光灯");
                    setting_name_powertype.setText("电池类型");
                    setting_name_cameraline.setText("辅助线");
                    setting_name_shuiping.setText("水平线");
                    if (splash_text.getText().equals("closed")){
                        splash_text.setText("已关闭");
                    }else if (splash_text.getText().equals("auto")){
                        splash_text.setText("自动");
                    }else {
                        splash_text.setText("已开启");
                    }
                    if (camera_line_text.getText().equals("opened")){
                        camera_line_text.setText("已开启");
                    }else{
                        camera_line_text.setText("已关闭");
                    }
                    if (text_fangdou.getText().equals("opened")){
                        text_fangdou.setText("已开启");
                    }else {
                        text_fangdou.setText("已关闭");
                    }
                    if (setting_second_state_dir.getText().equals("opposite direction")){
                        setting_second_state_dir.setText("反向");
                    }else {
                        setting_second_state_dir.setText("正向");
                    }
                    if (setting_second_state_bluetooth.getText().equals("connected")){
                        setting_second_state_bluetooth.setText("已连接");
                    }else{
                        setting_second_state_bluetooth.setText("未连接");
                    }
                    if (setting_second_state_shuiping.getText().equals("opened")){
                        setting_second_state_shuiping.setText("已开启");
                    }else {
                        setting_second_state_shuiping.setText("已关闭");
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                String fbl_fps = data.getStringExtra("FBL_FPS");
                setting_second_state_vedio.setText(fbl_fps);
                break;
        }
    }

    /*
        * 转换摄像头
         */
    private void changeCamera() {
        mFocusImage.stopFocus();
        PermissionUtil permissionUtil = new PermissionUtil(this);
        //若没有权限
        if (!permissionUtil.hasPermissionGranted(PICTURE_PERMISSIONS)) {
            //请求所需权限
            permissionUtil.requestRequiredPermissions(PICTURE_PERMISSIONS, R.string.need_permissions, REQUEST_PICTURE_PERMISSION);
            return;
        }
        //关闭相机再开启另外个摄像头
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        //获得Camera的系统服务管理器
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //获得指定CameraId相机设备的属性
        CameraCharacteristics cameraCharacteristics = null;
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //获得流配置
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //获取摄像头支持的最大尺寸
        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
        //创建一个ImageReader对象，用于获取摄像头的图像数据。设置图片大小为largest
        mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
        try {
            //若超过2500毫秒，Camera仍未打开
            if (!mCameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("相机打开超时");
            }

            //打开Camera
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            startPreview();
        } catch (InterruptedException e) {
            throw new RuntimeException("打开相机时中断");
        } catch (CameraAccessException e) {
            throw new RuntimeException("无法访问相机");
        }
    }


    private void openFlash(){
        if (cTime_splash % 3 == 1) {//已开启
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            } else if (cTime_splash % 3 == 2) {//自动
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
            } else {//已关闭
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
        updatePreview();
    }

    private  void closeFlash(){
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        updatePreview();
    }
    /**
     * 拍照
     */
    private void takePicture() {

//        WindowManager wm = (WindowManager) getApplicationContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//
//        int width = wm.getDefaultDisplay().getWidth();
//        int height = wm.getDefaultDisplay().getHeight();
//        Instrumentation mInst = new Instrumentation();
//        mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
//                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, width/2, height/2, 0));
//        mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
//                SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, width/2, height/2, 0));


        File file = new File(Environment.getExternalStorageDirectory().toString() + "/SmartCamera");
        if (!file.exists()) {
            file.mkdirs();
        }
        fileName = FileUtil.getFileName(true);
        //照片保存路径
        mFile = new File(Environment.getExternalStorageDirectory().toString() + "/SmartCamera", fileName);

        new File(Environment.getExternalStorageDirectory().toString());
        if (mCameraDevice == null) {
            return;
        }

        try {
            //创建作为拍照的CaptureRequest.Builder
            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //将mImageReader的surface作为CaptureRequest.Builder的目标
            mCaptureBuilder.addTarget(mImageReader.getSurface());

            //设置AF、AE模式
            setBuilder(mPreviewRequestBuilder);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            openFlash();
            previewBuilder2CaptureBuilder();
            //获得屏幕方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            characteristics = manager.getCameraCharacteristics(mCameraId);
            mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //获得Sensor方向
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            switch (mSensorOrientation) {
                //Sensor方向为90度时
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    //根据屏幕方向设置照片的方向
                    mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                //Sensor方向为270度时
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    //根据屏幕方向设置照片的方向
                    mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        //创建拍照的CameraCaptureSession.CaptureCallback对象
                        CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                                super.onCaptureStarted(session, request, timestamp, frameNumber);
                                updatePreview();//继续预览
                                mPlayer.start();
                            }
                            //在拍照完成时调用
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                Toast.makeText(CameraActivity.this, "图片正在保存，请稍后......", Toast.LENGTH_SHORT).show();
                                sleepTime=0;
                                try {
                                    MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),Environment.getExternalStorageDirectory().toString() + "/SmartCamera", fileName,null);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                // 最后通知图库更新
                                getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/SmartCamera/" + fileName)));

                            }
                        };
                        //停止连续取景
                        try {
                            mCameraCaptureSession.stopRepeating();
                            //捕获静态图像
                            mCameraCaptureSession.capture(mCaptureBuilder.build(), captureCallback, mBackgroundHandler);
                            closeFlash();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 这样做是为了获得mFocusImage的高度和宽度
     */
    private void initFocusImage() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mFocusImage.setLayoutParams(layoutParams);
        mFocusImage.initFocus();
    }
    /**
     * 生成PreviewSession对象
     */
    private void newPreviewSession() {
        mPreviewSessionCallback = new PreviewSessionCallback(mFocusImage, mMainHandler, mTextureView);
    }
    /**
     * 将previewBuilder中修改的参数设置到captureBuilder中
     */
    private void previewBuilder2CaptureBuilder() {
        //AWB
        mCaptureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AWB_MODE));
        //AE
//        if (mPreviewBuilder.get(CaptureRequest.CONTROL_AE_MODE) == CameraMetadata.CONTROL_AE_MODE_OFF) {
        //曝光时间
        mCaptureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mPreviewRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME));
//        } else if (mPreviewBuilder.get(CaptureRequest.CONTROL_AE_MODE) == CameraMetadata.CONTROL_AE_MODE_ON) {
        //曝光增益
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION));
//        }
        //AF
//        if (mPreviewBuilder.get(CaptureRequest.CONTROL_AF_MODE) == CameraMetadata.CONTROL_AF_MODE_OFF) {
        //手动聚焦的值
        mCaptureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mPreviewRequestBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE));
//        }
        //effects
        mCaptureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE));
        //ISO
        mCaptureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mPreviewRequestBuilder.get(CaptureRequest.SENSOR_SENSITIVITY));
        //AF REGIONS
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS));
//        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        //AE REGIONS
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_REGIONS));
//        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        //SCENSE
        mCaptureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, mPreviewRequestBuilder.get(CaptureRequest.CONTROL_SCENE_MODE));
        //zoom
        mCaptureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mPreviewRequestBuilder.get(CaptureRequest.SCALER_CROP_REGION));
    }
    @Override
    protected void onResume() {
        super.onResume();
        //开启后台线程
        startBackgroundThread();
        startPreview();
        if (mCameraId==null){
            mCameraId="0";
        }
        if (mCameraCaptureSession!=null && mPreviewRequestBuilder!=null){
            updatePreview();
        }
        if (mTextureView.isAvailable()) {
            //TextureView可用时，打开相机
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            //TextureView不可用时，为TextureView设置监听器
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        //屏幕旋转时，刷新相片预览方向的timer
//        Timer timer1 = new Timer();
//        TimerTask timerTask1 = new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mTextureView.isAvailable()) {
//                            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
//                        }else {
//                            //TextureView不可用时，为TextureView设置监听器
//                            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//                        }
//                    }
//                });
//            }
//        };
//        timer1.schedule(timerTask1,100,100);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
        //动态刷新蓝牙设备发送来的参数
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            private int power;
            private int progress, progress1;
            private int anTime, anTime1;
            private String status;

            @Override
            public void run() {

                SharedPreferences preferences = getSharedPreferences("status", Context.MODE_PRIVATE);
                status = preferences.getString("status", "未连接");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (setting_second_state_bluetooth!=null){
                        if (cTime_language%2==1){
                            if (status.equals("已连接")){
                                setting_second_state_bluetooth.setText("connected");
                            }else {
                                setting_second_state_bluetooth.setText("disconnected");
                            }
                        }else {
                            if (status.equals("已连接")){
                                setting_second_state_bluetooth.setText("已连接");
                            }else {
                                setting_second_state_bluetooth.setText("未连接");
                            }
                        }
                    }
                    }
                });
                SharedPreferences preferences_key = getSharedPreferences("Key", Context.MODE_PRIVATE);
                int key = preferences_key.getInt("Key", 0);
                anTime = preferences_key.getInt("anTime", 0);//addTime
                anTime1 = preferences_key.getInt("anTime1", 0);
                if (key == 12) {
                    SharedPreferences.Editor edit = preferences_key.edit();
                    edit.putInt("Key", 0);
                    edit.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_xiangji.performClick();
                        }
                    });
                }
                if (key == 3) {//控制es ev切换
                    if (anTime > 0) {
                        SharedPreferences.Editor edit = preferences_key.edit();
                        edit.putInt("Key", 0);
                        edit.commit();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_ev.performClick();
                            }
                        });
                        edit.putInt("anTime", 0);
                        edit.commit();
                    }
                }
                if (key == 8) {//控制zoom mf切换
                    if (anTime1 > 0) {
                        SharedPreferences.Editor edit = preferences_key.edit();
                        edit.putInt("Key", 0);
                        edit.commit();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_zoom.performClick();
                            }
                        });
                        edit.putInt("anTime1", 0);
                        edit.commit();
                    }
                }
                if (key == 1 || key == 4) {//调小 左侧
                    progress = sb_ev.getProgress();
                    progress = progress - 5;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb_ev.setProgress(progress);
                        }
                    });
                    SharedPreferences.Editor edit = preferences_key.edit();
                    edit.putInt("Key", 0);
                    edit.commit();
                }
                if (key == 2 || key == 5) {//调大 左侧
                    progress = sb_ev.getProgress();
                    progress = progress + 5;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb_ev.setProgress(progress);
                        }
                    });
                    SharedPreferences.Editor edit = preferences_key.edit();
                    edit.putInt("Key", 0);
                    edit.commit();
                }
                if (key == 6 || key == 9) {//调小 右侧
                    progress1 = sb_zoom.getProgress();
                    progress1 = progress1 - 10;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb_zoom.setProgress(progress1);
                        }
                    });
                    SharedPreferences.Editor edit = preferences_key.edit();
                    edit.putInt("Key", 0);
                    edit.commit();
                }
                if (key == 7 || key == 10) {//调大 右侧
                    progress1 = sb_zoom.getProgress();
                    progress1 = progress1 + 10;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb_zoom.setProgress(progress1);
                        }
                    });
                    SharedPreferences.Editor edit = preferences_key.edit();
                    edit.putInt("Key", 0);
                    edit.commit();
                }
                SharedPreferences preferences_Power = getSharedPreferences("Power", Context.MODE_PRIVATE);
                power = preferences_Power.getInt("power", 0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        power_text.setText(power +"%");
                    }
                });
            }

        };
        timer.schedule(timerTask, 500, 500);
    }

    @Override
    protected void onDestroy() {
        if (sm!=null){
            sm.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //关闭摄像头
        closeCamera();
        //停止后台线程
        stopBackgroundThread();
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                String s = String.valueOf(level);
                Message msg = new Message();
                msg.what = MSG_REFRESH_UI;
                msg.obj = s;
                mhHandler.sendMessage(msg);
            }
        }
    };

    /**
     * 开启后台线程
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 停止后台线程
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("停止后台线程时中断");
        }
    }

    /**
     * 关闭摄像头
     */
    private void closeCamera() {
        try {
            mCameraLock.acquire();
            //关闭CameraCaptureSession
            closeCameraCaptureSession();
            //关闭CameraDevice
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;

            }
            //关闭ImageReader
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("关闭相机时中断");
        } finally {
            mCameraLock.release();

        }
    }

    /**
     * 关闭CameraCaptureSession
     */
    private void closeCameraCaptureSession() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
    }

    /**
     * activity的onRequestPermissionsResult会被回调来通知结果（通过第三个参数）
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //匹配请求码
        switch (requestCode) {
            case REQUEST_PICTURE_PERMISSION:
                if (grantResults.length == PICTURE_PERMISSIONS.length) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            stopApp(this);//若未被赋予相应权限，APP停止运行
                            break;
                        }
                    }
                } else {
                    stopApp(this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 停止Activity：APP停止运行
     */
    private void stopApp(Activity activity) {
        Toast.makeText(activity, R.string.sorry, Toast.LENGTH_SHORT).show();
        activity.finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }

        float[] values = event.values;
        float ax = values[0];
        float ay = values[1];

        double g = Math.sqrt(ax * ax + ay * ay);
        double cos = ay / g;
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double rad = Math.acos(cos);
        if (ax < 0) {
            rad = 2 * Math.PI - rad;
        }

        int uiRot = getWindowManager().getDefaultDisplay().getRotation();
        double uiRad = Math.PI / 2 * uiRot;
        rad -= uiRad;

        gSensitiveView.setRotation(rad);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class MyBanSeekBarListener implements BanSeekBar.OnBanSeekBarChangeListener {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
            try {
                if (mCameraId != null) {
                    characteristics = cameraManager.getCameraCharacteristics(mCameraId);

                    //Range<Integer>[] ranges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                }

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            SharedPreferences sp_seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp_seekbar_value.edit();
            switch (seekBar.getId()) {
                case R.id.buchang:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                    if (range1 != null) {
                        int maxmax = range1.getUpper();
                        int minmin = range1.getLower();
                        int all = (-minmin) + maxmax;
                        int time = 100 / all;
                        int ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                        Log.e(TAG, "ae: " + ae);
                        updatePreview();//更新预览
                        if (seekTime % 3 == 0 ){
                            sb_ev.setProgress(i);
                        }
                        edit.putInt("ev",i);
                        edit.putString("ev_text",CameraActivity.this.ae+"");
                    }
                    edit.commit();
                    break;
                case R.id.kuaimen:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    //ES
                    //曝光时间
                    if (seekBar.getId() != R.id.buchang) {
                        Range<Long> rangeTime = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                        if (rangeTime != null) {
                            long max = rangeTime.getUpper();
                            long min = rangeTime.getLower();
                            max= max / 100> min ? max / 100 : max;
                            long aet = ((i * (max - min)) / 100 + min);
                            mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, aet);
                            flag1 = false;
                            updatePreview();//更新预览
                            if (seekTime % 3 == 1 ){
                                sb_ev.setProgress(i);
                            }
                            edit.putInt("es",i);
                            edit.putString("es_text",i+"");
                        }
                    }
                    edit.commit();
                    break;
                case R.id.sewen:
                    int[] awb = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                    int wb = i / 11;
                    if (i == 9) {
                        wb = 8;
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, wb);
                    updatePreview();//更新预览
                    if (seekTime2 % 3 == 2) {
                        sb_zoom.setProgress(i);
                    }
                    edit.putInt("wb",i);
                    edit.putString("wb_text",wb+"");
                    edit.commit();
                    break;
                case R.id.sb_iso:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    Range<Integer> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    if (range != null) {
                        int max1 = range.getUpper();//>=800
                        int min1 = range.getLower();//<=100
                        int iso = ((i * (max1 - min1)) / 100 + min1);
                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                        valueISO = iso;
                        tv_iso.setText(iso+"");
                        updatePreview();//更新预览
                        if (seekTime % 3 == 2 ){
                            sb_ev.setProgress(i);
                        }
                        edit.putInt("iso",i);
                        edit.putString("iso_text",iso+"");
                        edit.commit();
                    }
                    break;
            }
        }
    }

    /**
     * seekbar的监听器
     */
    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {


        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            try {
                if (mCameraId != null) {
                    characteristics = cameraManager.getCameraCharacteristics(mCameraId);

                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            SharedPreferences sp_seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp_seekbar_value.edit();
            switch (seekBar.getId()) {
                case R.id.sb_zoom:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    if (seekTime2 % 3 == 0) {
                        //ZOOM
                        Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                        if (rect != null) {
                            int radio = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).intValue() / 3;
                            int realRadio = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)).intValue();
                            int centerX = rect.centerX();
                            int centerY = rect.centerY();
                            int minMidth = (rect.right - ((i * centerX) / 100 / radio) - 1) - ((i * centerX / radio) / 100 + 8);
                            int minHeight = (rect.bottom - ((i * centerY) / 100 / radio) - 1) - ((i * centerY / radio) / 100 + 16);
                            if (minMidth < rect.right / realRadio || minHeight < rect.bottom / realRadio) {
                                return;
                            }
                            Rect newRect = new Rect((i * centerX / radio) / 100 + 40, (i * centerY / radio) / 100 + 40, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
                            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
                            zoom_text.setText(i / 10 + ".0");
                            updatePreview();//更新预览
                            edit.putInt("zoom",i);
                            edit.putString("zoom_text",i/10+".0");
                        }
                    } else if (seekTime2 % 3 == 1) {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                        //MF
                        Float range1 = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                        if (range1 != null) {
                            float num = (((float) i) * range1 / 100);
                            mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, num);
                            int showNum = (int) num;
                            zoom_text.setText(showNum + "");
                            updatePreview();//更新预览
                            flag3 = false;
                            edit.putInt("mf",i);
                            edit.putString("mf_text",showNum+"");
                        }

                    } else {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (timer_wb != null) {
                            timer_wb.cancel();
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                        //WB CONTROL_AWB_MODE
                        int[] awb = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                        if (i<11){
                            i=11;
                        }
                        int wb = i / 11;
                        if (wb == 9) {
                            wb = 8;
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, wb);
                        zoom_text.setText(wb + "");
                        updatePreview();//更新预览
                        flag2 = false;
                        edit.putInt("wb",i);
                        edit.putString("wb_text",wb+"");
                    }
                    edit.commit();
                    break;
                case R.id.sb_ev:

                    if (seekTime % 3 == 0) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        //EV
                        //曝光补偿
                        Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                        if (range1 != null) {
                            int maxmax = range1.getUpper();
                            int minmin = range1.getLower();
                            int all = (-minmin) + maxmax;
                            int time = 100 / all;
                            ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);

                            es_text.setText(ae + "");
                            updatePreview();//更新预览
                            edit.putInt("ev",i);
                            edit.putString("ev_text",ae+"");

                        }
                    } else if (seekTime % 3 == 1) {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (timer_type != null) {
                            timer_type.cancel();
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        //ES
                        //曝光时间
                        if (seekBar.getId() != R.id.buchang) {
                            Range<Long> rangeTime = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                            if (rangeTime != null) {
                                long max = rangeTime.getUpper();
                                long min = rangeTime.getLower();
                                max= max / 100> min ? max / 100 : max;
                                long aet = ((i * (max - min)) / 100 + min);
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, aet);
                                es_text.setText(i + "");
                                flag1 = false;
                                updatePreview();//更新预览
                                edit.putInt("es",i);
                                edit.putString("es_text",i+"");
                            }
                        }
                    } else {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (timer_type != null) {
                            timer_type.cancel();
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        //Iso
                        //感光度
                        if (seekBar.getId() != R.id.buchang) {
                            Range<Integer> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                            if (range != null) {
                                int max1 = range.getUpper();//>=800
                                int min1 = range.getLower();//<=100
                                int iso = ((i * (max1 - min1)) / 100 + min1);
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                                valueISO = iso;
                                es_text.setText(iso+ "" );
//                                bansb_iso.setProgress(i);
                                flag1 = false;
                                updatePreview();//更新预览
                                edit.putInt("iso",i);
                                edit.putString("iso_text",iso+"");
                            }
                        }
                    }
                    edit.commit();
                    break;
                case R.id.buchang:
                    Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                    if (range1 != null) {
                        int maxmax = range1.getUpper();
                        int minmin = range1.getLower();
                        int all = (-minmin) + maxmax;
                        int time = 100 / all;
                        ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                        Log.e(TAG, "ae: " + ae);
                        updatePreview();//更新预览
                    }
                    break;


            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /*
       * 按两次Back键退出
       * */
    private long firstTime = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 5000) {                                         //如果两次按键时间间隔大于2秒，则不退出
                    Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;//更新firstTime
                    return true;
                } else {                                                    //两次按键小于2秒时，退出应用
                    ActivityCollector.finishAll();
                }
                break;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
//            closeCameraCaptureSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mVideoSize.getWidth(),mVideoSize.getHeight());
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewRequestBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewRequestBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    updatePreview();
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = this;
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");//设置日期格式
            String time = df.format(System.currentTimeMillis());
            mNextVideoAbsolutePath = "/storage/emulated/0/SmartCamera/VID_" + time + ".mp4";
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private void stopRecordingVideo() {
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Video saved: " + mNextVideoAbsolutePath,
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
            }
        });
        mNextVideoAbsolutePath = null;

        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            //TextureView可用时，打开相机
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            //TextureView不可用时，为TextureView设置监听器
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    private static class GSensitiveView extends ImageView {

        private Bitmap image;
        private double rotation;
        private Paint paint;

        public GSensitiveView(Context context) {
            super(context);
            BitmapDrawable drawble = (BitmapDrawable) context.getResources().getDrawable(R.drawable.zhixian);
            image = drawble.getBitmap();

            paint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // super.onDraw(canvas);

            double w = image.getWidth();
            double h = image.getHeight();

            Rect rect = new Rect();
            getDrawingRect(rect);

            int degrees = (int) (180 * rotation / Math.PI);
            canvas.rotate(degrees, rect.width() / 2, rect.height() / 2);
            canvas.drawBitmap(image, //
                    (float) ((rect.width() - w) / 2),//
                    (float) ((rect.height() - h) / 2),//
                    paint);
        }

        public void setRotation(double rad) {
            rotation = rad;
            invalidate();
        }

    }
    private ArrayList<String> getData(){
        ArrayList<String> data = new ArrayList<String>();

        data.add("3s");
        data.add("6s");
        data.add("10s");
        if (cTime_language%2==1){
            data.add("Cancel");
        }else {
            data.add("取消");
        }
        return data;
    }
    private void showPopupWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(CameraActivity.this).inflate(R.layout.layout_popwindow_yssy, null);
        mPopWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);

        ListView lv_popitem = (ListView)contentView.findViewById(R.id.lv_popitem);
        lv_popitem.setAdapter(new PopListAdaptor(getApplicationContext(),getData()));

        lv_popitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"倒计时拍照"+getData().get(position),Toast.LENGTH_SHORT).show();
                text_yssy = getData().get(position);
                mPopWindow.dismiss();
            }
        });

        //显示PopupWindow
        View rootview = LayoutInflater.from(CameraActivity.this).inflate(R.layout.activity_camera, null);
        mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }
    public final  class ViewHolder{
        public  TextView time_item_yssy;
    }

    public class PopListAdaptor extends BaseAdapter{

        private LayoutInflater mInflater;
        private Context context;
        private ArrayList<String> list;
        public PopListAdaptor(Context context,ArrayList<String> list) {
            this.mInflater = LayoutInflater.from(context);
            this.context = context;
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
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
            ViewHolder holder = null;
            if (convertView == null) {

                holder=new ViewHolder();

                convertView = mInflater.inflate(R.layout.layout_item_pop, null);
                holder.time_item_yssy = (TextView)convertView.findViewById(R.id.time_item_yssy);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.time_item_yssy.setText(list.get(position));
            return convertView;
        }
    }

    private void showSettingPopWindow() {
        //设置contentView
        View contentView = LayoutInflater.from(CameraActivity.this).inflate(R.layout.layout_popwindow_setting, null);
        mPopWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, 700, true);

        mPopWindow.setContentView(contentView);
        RelativeLayout layout_setting_bluetooth_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_bluetooth_include);
        RelativeLayout layout_setting_vedio_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_vedio_include);
        RelativeLayout layout_setting_handdirection_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_handdirection_include);
        RelativeLayout layout_setting_fangdou_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_fangdou_include);
        RelativeLayout layout_setting_splash_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_splash_include);
        RelativeLayout layout_setting_language_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_language_include);
        RelativeLayout layout_setting_power_type_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_power_type_include);
        RelativeLayout layout_setting_camerline_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_camerline_include);
        RelativeLayout layout_setting_shuiping_include = (RelativeLayout)contentView.findViewById(R.id.layout_setting_shuiping_include);
        layout_setting_bluetooth_include.setOnClickListener(this);
        layout_setting_vedio_include.setOnClickListener(this);
        layout_setting_handdirection_include.setOnClickListener(this);
        layout_setting_fangdou_include.setOnClickListener(this);
        layout_setting_splash_include.setOnClickListener(this);
        layout_setting_language_include.setOnClickListener(this);
        layout_setting_power_type_include.setOnClickListener(this);
        layout_setting_camerline_include.setOnClickListener(this);
        layout_setting_shuiping_include.setOnClickListener(this);

        setting_name_bluetooth = (TextView) contentView.findViewById(R.id.setting_name_bluetooth);
        setting_name_video = (TextView) contentView.findViewById(R.id.setting_name_video);
        setting_name_handdirection = (TextView) contentView.findViewById(R.id.setting_name_handdirection);
        setting_name_fangdou = (TextView) contentView.findViewById(R.id.setting_name_fangdou);
        setting_name_splash = (TextView) contentView.findViewById(R.id.setting_name_splash);
        setting_name_powertype = (TextView) contentView.findViewById(R.id.setting_name_powertype);
        setting_name_cameraline = (TextView) contentView.findViewById(R.id.setting_name_cameraline);
        setting_second_state_dir = (TextView) contentView.findViewById(R.id.setting_second_state_dir);
        setting_second_state_power_type = (TextView) contentView.findViewById(R.id.setting_second_state_power_type);
        camera_line_text = (TextView) contentView.findViewById(R.id.setting_second_state_line);
        splash_text = (TextView) contentView.findViewById(R.id.setting_splash_second_state);
        setting_second_state_shuiping = (TextView) contentView.findViewById(R.id.setting_second_state_shuiping);
        setting_name_shuiping = (TextView) contentView.findViewById(R.id.setting_name_shuiping);
        text_fangdou = (TextView) contentView.findViewById(R.id.setting_second_state_fangdou);
        setting_second_state_bluetooth = (TextView) contentView.findViewById(R.id.setting_second_state_bluetooth);
        setting_second_state_language = (TextView) contentView.findViewById(R.id.setting_second_state_language);
        setting_second_state_vedio = (TextView)contentView.findViewById(R.id.setting_second_state_vedio);

        /*闪光灯*/
        if (cTime_splash % 3 == 0) {//已关闭
            if (cTime_language%2==1){
                splash_text.setText("closed");
            }else {
                splash_text.setText("已关闭");
            }

        } else if (cTime_splash % 3 == 1) {//已开启
            if (cTime_language%2==1){
                splash_text.setText("opened");
            }else {
                splash_text.setText("已开启");
            }

        } else {//自动
            if (cTime_language%2==1){
                splash_text.setText("auto");
            }else {
                splash_text.setText("自动");
            }
        }
        /*防抖*/
        if (cTime_fd % 2 == 1) {
            if (cTime_language%2==1){
                text_fangdou.setText("opened");
            }else {
                text_fangdou.setText("已开启");
            }
        } else {
            if (cTime_language%2==1){
                text_fangdou.setText("closed");
            }else {
                text_fangdou.setText("已关闭");
            }
        }
        /*拍照辅助线*/
        if (cTime_cl % 2 == 1) {
            if (cTime_language%2==1){
                camera_line_text.setText("opened");
            }else {
                camera_line_text.setText("已开启");
            }

        } else {
            if (cTime_language%2==1){
                camera_line_text.setText("closed");
            }else {
                camera_line_text.setText("已关闭");
            }
        }
        /*重力水平线*/
        if (cTime_spx%2==1){
            if (cTime_language%2==1){
                setting_second_state_shuiping.setText("opened");
            }else {
                setting_second_state_shuiping.setText("已开启");
            }
        }else {
            if (cTime_language%2==1){
                setting_second_state_shuiping.setText("closed");
            }else {
                setting_second_state_shuiping.setText("已关闭");
            }
        }
        /*手轮方向*/
        if (cTime_dir%2==1){
            if (cTime_language%2==1){
                setting_second_state_dir.setText("opposite direction");
            }else {
                setting_second_state_dir.setText("反向");
            }
        }else{
            if (cTime_language%2==1){
                setting_second_state_dir.setText("forward direction");
            }else {
                setting_second_state_dir.setText("正向");
            }
        }

        if (cTime_language%2==1){
            setting_second_state_language.setText("English");
            //设置
            setting_name_bluetooth.setText("BlueTooth");
            setting_name_video.setText("VideoSetting");
            setting_name_handdirection.setText("Handwheel Direction");
            setting_name_fangdou.setText("Anti-shake");
            setting_name_splash.setText("Splash");
            setting_name_powertype.setText("Power Type");
            setting_name_cameraline.setText("Guide");
            setting_name_shuiping.setText("Gravity horizontal line");
            if (cTime_splash % 3 == 0) {//已关闭
                splash_text.setText("closed");
            }else if (cTime_splash % 3 == 2){
                splash_text.setText("auto");
            }else {
                splash_text.setText("opened");
            }
            if (cTime_cl % 2 == 1) {
                camera_line_text.setText("opened");
            }else{
                camera_line_text.setText("closed");
            }
            if (cTime_fd % 2 == 1) {
                text_fangdou.setText("opened");
            }else {
                text_fangdou.setText("closed");
            }
            if (cTime_dir%2==1){
                setting_second_state_dir.setText("opposite direction");
            }else {
                setting_second_state_dir.setText("forward direction");
            }
            if (setting_second_state_bluetooth.getText().equals("未连接")){
                setting_second_state_bluetooth.setText("disconnected");
            }else{
                setting_second_state_bluetooth.setText("connected");
            }
            if (cTime_spx%2==1){
                setting_second_state_shuiping.setText("opened");
            }else {
                setting_second_state_shuiping.setText("closed");
            }
        }else {
            setting_second_state_language.setText("中文");
            //设置
            setting_name_bluetooth.setText("蓝牙");
            setting_name_video.setText("视频设置");
            setting_name_handdirection.setText("手轮方向");
            setting_name_fangdou.setText("防抖");
            setting_name_splash.setText("闪光灯");
            setting_name_powertype.setText("电池类型");
            setting_name_cameraline.setText("辅助线");
            setting_name_shuiping.setText("水平线");
            if (cTime_splash % 3 == 0) {//已关闭
                splash_text.setText("已关闭");
            }else if (cTime_splash % 3 == 2){
                splash_text.setText("自动");
            }else {
                splash_text.setText("已开启");
            }
            if (cTime_cl % 2 == 1) {
                camera_line_text.setText("已开启");
            }else{
                camera_line_text.setText("已关闭");
            }
            if (cTime_fd % 2 == 1) {
                text_fangdou.setText("已开启");
            }else {
                text_fangdou.setText("已关闭");
            }
            if (cTime_dir%2==1){
                setting_second_state_dir.setText("反向");
            }else {
                setting_second_state_dir.setText("正向");
            }
            if (setting_second_state_bluetooth.getText().equals("connected")){
                setting_second_state_bluetooth.setText("已连接");
            }else{
                setting_second_state_bluetooth.setText("未连接");
            }
            if (cTime_spx%2==1){
                setting_second_state_shuiping.setText("已开启");
            }else {
                setting_second_state_shuiping.setText("已关闭");
            }
        }
        //显示PopupWindow
        View rootview = LayoutInflater.from(CameraActivity.this).inflate(R.layout.activity_camera, null);
        mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }

    private void showIsoPopWindow() {
        SharedPreferences sp_seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
        String iso_text = sp_seekbar_value.getString("iso_text", "");
        int iso = sp_seekbar_value.getInt("iso", 0);

        //设置contentView
        View contentView = LayoutInflater.from(CameraActivity.this).inflate(R.layout.layout_pop_iso, null);
        mPopWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);

        bansb_iso = (BanSeekBar) contentView.findViewById(R.id.sb_iso);
        bansb_iso.setOnBanSeekBarChangeListener(myBanSeekBarListener);
        tv_iso = (TextView) contentView.findViewById(R.id.tv_iso);
        bansb_iso.setProgress(iso);
        tv_iso.setText(iso_text);
        //显示PopupWindow
        View rootview = LayoutInflater.from(CameraActivity.this).inflate(R.layout.activity_camera, null);
        mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }

    private void showWBPopWindow() {
        SharedPreferences sp_seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
        int wb = sp_seekbar_value.getInt("wb", 0);

        //设置contentView
        View contentView = LayoutInflater.from(CameraActivity.this).inflate(R.layout.layout_pop_wb, null);
        mPopWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);

        tv_wb_mode = (TextView)contentView.findViewById(R.id.tv_wb_mode);
        tv_wb_mode_sewen = (TextView)contentView.findViewById(R.id.tv_wb_mode_sewen);
        tv_wb_mode_sediao = (TextView)contentView.findViewById(R.id.tv_wb_mode_sediao);
        matul_btn = (Button) contentView.findViewById(R.id.matul_btn);
        automatic = (Button) contentView.findViewById(R.id.automatic);
        bansb_sewen = (BanSeekBar) contentView.findViewById(R.id.sewen);
        bansb_sediao = (BanSeekBar) contentView.findViewById(R.id.sediao);
        bansb_sewen.setOnBanSeekBarChangeListener(myBanSeekBarListener);
        matul_btn.setOnClickListener(this);
        automatic.setOnClickListener(this);
        bansb_sewen.setProgress(wb);
        if (cTime_language%2==1){
            //WB
            tv_wb_mode.setText("Mode");
            tv_wb_mode_sewen.setText("ColorTemperature");
            tv_wb_mode_sediao.setText("Tone");
            automatic.setText("auto");
            matul_btn.setText("matul");
        }else {
            //WB
            tv_wb_mode.setText("模式");
            tv_wb_mode_sewen.setText("色温");
            tv_wb_mode_sediao.setText("色调");
            automatic.setText("自动");
            matul_btn.setText("手动");
        }

        //显示PopupWindow
        View rootview = LayoutInflater.from(CameraActivity.this).inflate(R.layout.activity_camera, null);
        mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }

    private void showTypePopWindow() {
        SharedPreferences sp_seekbar_value = getSharedPreferences("seekbar_value", Context.MODE_PRIVATE);
        int ev = sp_seekbar_value.getInt("ev", 0);
        int es = sp_seekbar_value.getInt("es", 0);

        //设置contentView
        View contentView = LayoutInflater.from(CameraActivity.this).inflate(R.layout.layout_pop_type, null);
        mPopWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);


        tv_type = (TextView)contentView.findViewById(R.id.tv_type);
        tv_type_jizhun = (TextView)contentView.findViewById(R.id.tv_type_jizhun);
        tv_type_kuaimen = (TextView)contentView.findViewById(R.id.tv_type_kuaimen);
        tv_type_buchang = (TextView)contentView.findViewById(R.id.tv_type_buchang);
        type_matul_btn = (Button) contentView.findViewById(R.id.type_matul_btn);
        type_automatic = (Button) contentView.findViewById(R.id.type_automatic);
        lock_btn = (Button) contentView.findViewById(R.id.lock_btn);
        sb_jizhun = (BanSeekBar) contentView.findViewById(R.id.jizhun);
        sb_kuaimen = (BanSeekBar) contentView.findViewById(R.id.kuaimen);
        sb_buchang_type = (BanSeekBar) contentView.findViewById(R.id.buchang);


        sb_kuaimen.setProgress(es);
        sb_buchang_type.setProgress(ev);
        type_matul_btn.setOnClickListener(this);
        type_automatic.setOnClickListener(this);
        sb_buchang_type.setOnBanSeekBarChangeListener(myBanSeekBarListener);
        sb_kuaimen.setOnBanSeekBarChangeListener(myBanSeekBarListener);

        if (cTime_language%2==1){
            //小太阳
            tv_type.setText("Mode");
            tv_type_jizhun.setText("Benchmark");
            tv_type_kuaimen.setText("Shutter");
            tv_type_buchang.setText("LightCompensation");
            type_automatic.setText("auto");
            type_matul_btn.setText("matul");
            lock_btn.setText("lock");
        }else {
            //小太阳
            tv_type.setText("模式");
            tv_type_jizhun.setText("基准");
            tv_type_kuaimen.setText("快门");
            tv_type_buchang.setText("补偿");
            type_automatic.setText("自动");
            type_matul_btn.setText("手动");
            lock_btn.setText("锁定");
        }

        //显示PopupWindow
        View rootview = LayoutInflater.from(CameraActivity.this).inflate(R.layout.activity_camera, null);
        mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }
}
