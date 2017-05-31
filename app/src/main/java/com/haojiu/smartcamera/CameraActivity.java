package com.haojiu.smartcamera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by leehom on 2017/4/6.
 */

public class CameraActivity extends Activity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

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
    private Size mPreviewSize;//预览大小
    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;//代表摄像头的成员变量
    private String mCameraId = "0";//摄像头ID（通常0代表后置摄像头，1代表前置摄像头）
    private Semaphore mCameraLock = new Semaphore(1);//Camera互斥锁
    private int valueAE, valueISO, sleepTime = 0;
    private Boolean flag1 = true, flag2 = true, flag3 = true;//lag1 与ES/ISO 对应，flag2与WB对应，flag3 与MF对应
    private SeekBar sb_ev;
    private CameraCharacteristics characteristics = null;
    private CameraManager cameraManager;
    private TextView zoom_text;
    private TextView es_text;
    private ImageButton btn_iso;
    private LinearLayout layout_iso_include;
    private Button close_iso;
    private TextView tv_iso;
    private MySeekBarListener mySeekBarListener;
    private LinearLayout layout_awb_include;
    private Button close_awb;
    private ImageButton btn_wb;
    private ImageButton btn_zipai;
    private ImageButton btn_auto;
    private ImageButton btn_style;
    private ImageButton btn_setting;
    private ImageButton btn_luxiang;
    private ImageButton btn_xiangce;
    private ImageButton btn_disp;
    int cTime = 1, cTime_cl, cTime_fd, cTime_splash,cTime_language,cTime_dir, seekTime = 0, seekTime2 = 0;
    private RelativeLayout layout_setting_include;
    private Button close_setting;
    private TextView tv_yanchi;
    private Button btn_minus;
    private Button btn_add;
    private Timer timer, timer_type, timer_wb;
    private TimerTask task, task_type, task_wb;
    private CameraLine camera_line;
    private RelativeLayout cameraline_include;
    private TextView camera_line_text;
    private RelativeLayout splash_include;
    private TextView splash_text;
    private ImageButton btn_ev;
    private ImageButton btn_zoom;
    private Button matul_btn;
    private Button automatic;
    private ImageButton btn_xiangji;
    private int ae;
    private String mNextVideoAbsolutePath;
    private Size mVideoSize;
    protected static final int MSG_REFRESH_UI = 1000;//电池电量
    //拍照权限请求码
    private static final int REQUEST_PICTURE_PERMISSION = 1;
    //拍照权限
    private static final String[] PICTURE_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

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
    private LinearLayout layout_type_include;
    private Button btn_close_type;
    private Button type_matul_btn;
    private Button type_automatic;
    private RelativeLayout fangdou_include;
    private TextView text_fangdou;
    private TextView setting_second_state;
    private RelativeLayout layout_setting_bluetooth_include;
    private BanSeekBar sb_zoom;
    private BanSeekBar sb_jizhun;
    private BanSeekBar sb_kuaimen;
    private BanSeekBar sb_buchang_type;
    private MyBanSeekBarListener myBanSeekBarListener;
    private BanSeekBar bansb_sewen;
    private BanSeekBar bansb_sediao;
    private BanSeekBar bansb_iso;
    private TextView setting_second_state_bluetooth;
    private MediaRecorder mMediaRecorder;
    private Button luxiang_stop;
    private RelativeLayout layout_setting_language_include;
    private TextView setting_second_state_language;
    private Button lock_btn;
    private TextView tv_type;
    private TextView tv_type_jizhun;
    private TextView tv_type_kuaimen;
    private TextView tv_type_buchang;
    private TextView tv_wb_mode;
    private TextView tv_wb_mode_sewen;
    private TextView tv_wb_mode_sediao;
    private TextView tv_power;
    private TextView setting_name_bluetooth;
    private TextView setting_name_video;
    private TextView setting_name_handdirection;
    private TextView setting_name_fangdou;
    private TextView setting_name_splash;
    private TextView setting_name_powertype;
    private TextView setting_name_cameraline;
    private TextView tv_setting_timeout;
    private TextView setting_second_state_dir;
    private RelativeLayout layout_setting_handdirection_include;
    private TextView setting_second_state_power_type;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        // 显示界面
        setContentView(R.layout.activity_camera);
        luxiang_stop = (Button) findViewById(R.id.luxiang_stop);
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        zoom_text = (TextView) findViewById(R.id.zoom_text);
        es_text = (TextView) findViewById(R.id.es_text);
        mButton = (ImageButton) findViewById(R.id.picture);
        btn_iso = (ImageButton) findViewById(R.id.iso);
        tv_iso = (TextView) findViewById(R.id.tv_iso);
        layout_iso_include = (LinearLayout) findViewById(R.id.layout_iso_include);
        bansb_iso = (BanSeekBar) findViewById(R.id.sb_iso);
        sb_ev = (SeekBar) findViewById(R.id.sb_ev);
        sb_zoom = (BanSeekBar) findViewById(R.id.sb_zoom);
        close_iso = (Button) findViewById(R.id.close_iso);
        layout_awb_include = (LinearLayout) findViewById(R.id.layout_awb_include);
        layout_setting_include = (RelativeLayout) findViewById(R.id.layout_setting_include);
        layout_type_include = (LinearLayout) findViewById(R.id.layout_type_include);
        close_awb = (Button) findViewById(R.id.close_awb);
        btn_wb = (ImageButton) findViewById(R.id.wb);
        btn_auto = (ImageButton) findViewById(R.id.auto);
        btn_style = (ImageButton) findViewById(R.id.style);
        btn_setting = (ImageButton) findViewById(R.id.setting);
        btn_luxiang = (ImageButton) findViewById(R.id.luxiang);
        btn_xiangce = (ImageButton) findViewById(R.id.xiangce);
        btn_zipai = (ImageButton) findViewById(R.id.zipai);
        btn_disp = (ImageButton) findViewById(R.id.disp);
        bansb_sewen = (BanSeekBar) findViewById(R.id.sewen);
        bansb_sediao = (BanSeekBar) findViewById(R.id.sediao);
        close_setting = (Button) findViewById(R.id.close_setting);
        tv_yanchi = (TextView) findViewById(R.id.tv_yanchi);
        btn_minus = (Button) findViewById(R.id.btn_minus);
        btn_add = (Button) findViewById(R.id.btn_add);
        camera_line = (CameraLine) findViewById(R.id.id_cl);
        cameraline_include = (RelativeLayout) findViewById(R.id.layout_setting_camerline_include);
        fangdou_include = (RelativeLayout) findViewById(R.id.layout_setting_fangdou_include);
        camera_line_text = (TextView) findViewById(R.id.setting_second_state_line);
        splash_include = (RelativeLayout) findViewById(R.id.layout_setting_splash_include);
        splash_text = (TextView) findViewById(R.id.setting_splash_second_state);
        btn_ev = (ImageButton) findViewById(R.id.ev);
        btn_zoom = (ImageButton) findViewById(R.id.zoom);
        matul_btn = (Button) findViewById(R.id.matul_btn);
        automatic = (Button) findViewById(R.id.automatic);
        type_matul_btn = (Button) findViewById(R.id.type_matul_btn);
        type_automatic = (Button) findViewById(R.id.type_automatic);
        lock_btn = (Button) findViewById(R.id.lock_btn);
        btn_xiangji = (ImageButton) findViewById(R.id.xiangji);
        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        power_text = (TextView) findViewById(R.id.power_text);
        tv_power = (TextView)findViewById(R.id.tv_power);
        romsize_text = (TextView) findViewById(R.id.romsize_text);
        btn_fankui = (Button) findViewById(R.id.fankui);
        rl_setting = (RelativeLayout) findViewById(R.id.rl_setting);
        btn_close_type = (Button) findViewById(R.id.close_type);
        text_fangdou = (TextView) findViewById(R.id.setting_second_state_fangdou);
        setting_second_state = (TextView) findViewById(R.id.setting_second_state);
        layout_setting_bluetooth_include = (RelativeLayout) findViewById(R.id.layout_setting_bluetooth_include);
        sb_jizhun = (BanSeekBar) findViewById(R.id.jizhun);
        sb_kuaimen = (BanSeekBar) findViewById(R.id.kuaimen);
        sb_buchang_type = (BanSeekBar) findViewById(R.id.buchang);
        setting_second_state_bluetooth = (TextView) findViewById(R.id.setting_second_state_bluetooth);
        layout_setting_language_include = (RelativeLayout)findViewById(R.id.layout_setting_language_include);
        setting_second_state_language = (TextView)findViewById(R.id.setting_second_state_language);
        tv_type = (TextView)findViewById(R.id.tv_type);
        tv_type_jizhun = (TextView)findViewById(R.id.tv_type_jizhun);
        tv_type_kuaimen = (TextView)findViewById(R.id.tv_type_kuaimen);
        tv_type_buchang = (TextView)findViewById(R.id.tv_type_buchang);
        tv_wb_mode = (TextView)findViewById(R.id.tv_wb_mode);
        tv_wb_mode_sewen = (TextView)findViewById(R.id.tv_wb_mode_sewen);
        tv_wb_mode_sediao = (TextView)findViewById(R.id.tv_wb_mode_sediao);
        setting_name_bluetooth = (TextView)findViewById(R.id.setting_name_bluetooth);
        setting_name_video = (TextView)findViewById(R.id.setting_name_video);
        setting_name_handdirection = (TextView)findViewById(R.id.setting_name_handdirection);
        setting_name_fangdou = (TextView)findViewById(R.id.setting_name_fangdou);
        setting_name_splash = (TextView)findViewById(R.id.setting_name_splash);
        setting_name_powertype = (TextView)findViewById(R.id.setting_name_powertype);
        setting_name_cameraline = (TextView)findViewById(R.id.setting_name_cameraline);
        tv_setting_timeout = (TextView)findViewById(R.id.tv_setting_timeout);
        setting_second_state_dir = (TextView)findViewById(R.id.setting_second_state_dir);
        layout_setting_handdirection_include = (RelativeLayout)findViewById(R.id.layout_setting_handdirection_include);
        setting_second_state_power_type = (TextView)findViewById(R.id.setting_second_state_power_type);

        sb_ev.setMax(100);
        sb_zoom.setMax(100);
        sb_ev.setProgress(50);
        sb_zoom.setProgress(0);
        valueAE = 0;
        valueISO = 0;
        //button点击事件
        layout_setting_handdirection_include.setOnClickListener(this);
        layout_setting_language_include.setOnClickListener(this);
        btn_luxiang.setOnClickListener(this);
        luxiang_stop.setOnClickListener(this);
        fangdou_include.setOnClickListener(this);
        cameraline_include.setOnClickListener(this);
        splash_include.setOnClickListener(this);
        mButton.setOnClickListener(this);
        btn_iso.setOnClickListener(this);
        close_iso.setOnClickListener(this);
        close_awb.setOnClickListener(this);
        btn_wb.setOnClickListener(this);
        btn_zipai.setOnClickListener(this);
        btn_disp.setOnClickListener(this);
        btn_setting.setOnClickListener(this);
        close_setting.setOnClickListener(this);
        btn_minus.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_ev.setOnClickListener(this);
        btn_zoom.setOnClickListener(this);
        btn_auto.setOnClickListener(this);
        matul_btn.setOnClickListener(this);
        automatic.setOnClickListener(this);
        btn_fankui.setOnClickListener(this);
        btn_xiangce.setOnClickListener(this);
        btn_style.setOnClickListener(this);
        btn_close_type.setOnClickListener(this);
        type_matul_btn.setOnClickListener(this);
        type_automatic.setOnClickListener(this);
        layout_setting_bluetooth_include.setOnClickListener(this);
        //seekBar监听器
        mySeekBarListener = new MySeekBarListener();
        myBanSeekBarListener = new MyBanSeekBarListener();
        String sdAvailableSize = new Utils(getApplicationContext()).getSDAvailableSize();
        String romTotalSize = new Utils(getApplicationContext()).getRomTotalSize();
        romsize_text.setText(sdAvailableSize + "/" + romTotalSize);
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            mDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
//        }

    }

    //获取手机电量
    Handler mhHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    power_text.setText( msg.obj.toString() + "%");
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
            String cameraId = cameraManager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            //打开Camera
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            configureTransform(width, height);
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
//            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, valueAE);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mPreviewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, PRIVIEW_ORIENTATIONS.get(rotation));

            //将mTextureView的surface作为CaptureRequest.Builder的目标
            mPreviewRequestBuilder.addTarget(surface);

            //创建用于预览和拍照的CameraCaptureSession

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    mCameraCaptureSession = cameraCaptureSession;

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
                    mCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
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
//        if (mFlashSupported) {
//            //设置自动曝光模式
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//        }
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
            for (String cameraId : cameraIds) {
                //获得指定CameraId相机设备的属性
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                //获得摄像头朝向
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                //若是前置摄像头，不做任何操作
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                mCameraId = cameraId;
                //获得流配置
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }
                //获取摄像头支持的最大尺寸
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
                //创建一个ImageReader对象，用于获取摄像头的图像数据。设置图片大小为largest
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
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
                bansb_iso.setOnBanSeekBarChangeListener(myBanSeekBarListener);
                bansb_sewen.setOnBanSeekBarChangeListener(myBanSeekBarListener);
                sb_buchang_type.setOnBanSeekBarChangeListener(myBanSeekBarListener);
                //是否有闪光灯
//                Boolean available = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
//                mFlashSupported = (available == null ? false : available);
            }
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
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * 选择最佳大小
     */
    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        //若有足够大的，选择最小的一个
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture:
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(sleepTime * 1000);
                                    takePicture();//拍照
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
                            ae = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);

                            if (seekTime % 3 == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sb_ev.setProgress(ae.intValue() * (100 / 24) + 50);
                                        es_text.setText(ae + "");
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
                layout_type_include.setVisibility(View.VISIBLE);
                btn_wb.setClickable(false);
                btn_setting.setClickable(false);
                btn_zipai.setClickable(false);
                btn_xiangce.setClickable(false);
                btn_luxiang.setClickable(false);
                mButton.setClickable(false);
                btn_iso.setClickable(false);
                break;
            case R.id.close_type:
                layout_type_include.setVisibility(View.INVISIBLE);
                btn_wb.setClickable(true);
                btn_setting.setClickable(true);
                btn_zipai.setClickable(true);
                btn_xiangce.setClickable(true);
                btn_luxiang.setClickable(true);
                mButton.setClickable(true);
                btn_iso.setClickable(true);
                break;
            case R.id.iso:
                layout_iso_include.setVisibility(View.VISIBLE);
                btn_wb.setClickable(false);
                btn_style.setClickable(false);
                btn_setting.setClickable(false);
                btn_zipai.setClickable(false);
                btn_xiangce.setClickable(false);
                btn_luxiang.setClickable(false);
                mButton.setClickable(false);
                break;
            case R.id.close_iso:
                layout_iso_include.setVisibility(View.GONE);
                btn_wb.setClickable(true);
                btn_style.setClickable(true);
                btn_setting.setClickable(true);
                btn_zipai.setClickable(true);
                btn_xiangce.setClickable(true);
                btn_luxiang.setClickable(true);
                mButton.setClickable(true);
                break;
            case R.id.wb:
                layout_awb_include.setVisibility(View.VISIBLE);
                btn_iso.setClickable(false);
                btn_style.setClickable(false);
                btn_setting.setClickable(false);
                btn_zipai.setClickable(false);
                btn_xiangce.setClickable(false);
                btn_luxiang.setClickable(false);
                mButton.setClickable(false);
                break;
            case R.id.close_awb:
                layout_awb_include.setVisibility(View.GONE);
                btn_iso.setClickable(true);
                btn_style.setClickable(true);
                btn_setting.setClickable(true);
                btn_zipai.setClickable(true);
                btn_xiangce.setClickable(true);
                btn_luxiang.setClickable(true);
                mButton.setClickable(true);
                break;
            case R.id.zipai:
                if (Utils.isFastClick()) {
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
                layout_setting_include.setVisibility(View.VISIBLE);
                btn_wb.setClickable(false);
                btn_iso.setClickable(false);
                btn_style.setClickable(false);
                btn_zipai.setClickable(false);
                btn_xiangce.setClickable(false);
                btn_luxiang.setClickable(false);
                mButton.setClickable(false);
                String text = tv_yanchi.getText().toString();
                sleepTime = Integer.parseInt(text);
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
                        camera_line_text.setText("已打开");
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

                break;
            case R.id.layout_setting_splash_include://闪光灯设置
                if (cTime_splash % 3 == 0) {//已开启
                    if (cTime_language%2==1){
                        splash_text.setText("opened");
                    }else {
                        splash_text.setText("已开启");
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                } else if (cTime_splash % 3 == 1) {//自动
                    if (cTime_language%2==1){
                        splash_text.setText("auto");
                    }else {
                        splash_text.setText("自动");
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                } else {//已关闭
                    if (cTime_language%2==1){
                        splash_text.setText("closed");
                    }else {
                        splash_text.setText("已关闭");
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                }
                cTime_splash++;
                break;
            case R.id.close_setting:
                layout_setting_include.setVisibility(View.GONE);
                btn_wb.setClickable(true);
                btn_iso.setClickable(true);
                btn_style.setClickable(true);
                btn_zipai.setClickable(true);
                btn_xiangce.setClickable(true);
                btn_luxiang.setClickable(true);
                mButton.setClickable(true);
                break;
            case R.id.btn_add:
                if (sleepTime < 10) {
                    sleepTime++;
                    tv_yanchi.setText(sleepTime + "");
                }
                break;
            case R.id.btn_minus:
                if (sleepTime > 0) {
                    sleepTime--;
                    tv_yanchi.setText(sleepTime + "");
                }
                break;

            case R.id.ev:
                seekTime++;
                if (seekTime % 3 == 0) {
                    //ev
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_ev));
                } else if (seekTime % 3 == 1) {
                    //es
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_es));
                } else {
                    //iso
                    btn_ev.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_iso));
                }
                break;
            case R.id.zoom:
                seekTime2++;
                if (seekTime2 % 3 == 0) {
                    //ZOOM
                    btn_zoom.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_zoom));
                } else if (seekTime2 % 3 == 1) {
                    //MF
                    btn_zoom.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.selector_mf));
                } else {
                    //WB
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
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                            0);

                } else {
                    startRecordingVideo();
                }

                btn_luxiang.setVisibility(View.GONE);
                luxiang_stop.setVisibility(View.VISIBLE);
                break;
            case R.id.luxiang_stop:
                stopRecordingVideo();
                btn_luxiang.setVisibility(View.VISIBLE);
                luxiang_stop.setVisibility(View.GONE);
                break;
            case R.id.layout_setting_language_include:
                cTime_language++;
                if (cTime_language%2==1){
                    setting_second_state_language.setText("English");
                    //小太阳
                    tv_type.setText("Mode");
                    tv_type_jizhun.setText("Benchmark");
                    tv_type_kuaimen.setText("Shutter");
                    tv_type_buchang.setText("LightCompensation");
                    type_automatic.setText("auto");
                    type_matul_btn.setText("matul");
                    lock_btn.setText("lock");
                    btn_close_type.setText("close");
                    //WB
                    tv_wb_mode.setText("Mode");
                    tv_wb_mode_sewen.setText("ColorTemperature");
                    tv_wb_mode_sediao.setText("Tone");
                    close_awb.setText("close");
                    automatic.setText("auto");
                    matul_btn.setText("matul");
                    //iso
                    close_iso.setText("close");
                    //电量
                    tv_power.setText("Power：");
                    //设置
                    setting_name_bluetooth.setText("BlueTooth");
                    setting_name_video.setText("VideoSetting");
                    setting_name_handdirection.setText("Handwheel Direction");
                    setting_name_fangdou.setText("Anti-shake");
                    setting_name_splash.setText("Splash");
                    setting_name_powertype.setText("Power Type");
                    setting_name_cameraline.setText("Guide");
                    tv_setting_timeout.setText("Time-lapse photography");
                    close_setting.setText("close");
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
                }else {
                    setting_second_state_language.setText("中文");
                    //小太阳
                    tv_type.setText("模式");
                    tv_type_jizhun.setText("基准");
                    tv_type_kuaimen.setText("快门");
                    tv_type_buchang.setText("补偿");
                    type_automatic.setText("自动");
                    type_matul_btn.setText("手动");
                    lock_btn.setText("锁定");
                    btn_close_type.setText("关闭");
                    //WB
                    tv_wb_mode.setText("模式");
                    tv_wb_mode_sewen.setText("色温");
                    tv_wb_mode_sediao.setText("色调");
                    close_awb.setText("关闭");
                    automatic.setText("自动");
                    matul_btn.setText("手动");
                    //iso
                    close_iso.setText("关闭");
                    //电量
                    tv_power.setText("电量：");
                    //设置
                    setting_name_bluetooth.setText("蓝牙");
                    setting_name_video.setText("视频设置");
                    setting_name_handdirection.setText("手轮方向");
                    setting_name_fangdou.setText("防抖");
                    setting_name_splash.setText("闪光灯");
                    setting_name_powertype.setText("电池类型");
                    setting_name_cameraline.setText("辅助线");
                    tv_setting_timeout.setText("延时摄影");
                    close_setting.setText("关闭");
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
                }
                break;
            default:
                if (!rl_setting.hasFocus()) {
                    layout_setting_include.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    /*
    * 转换摄像头
     */
    private void changeCamera() {
        PermissionUtil permissionUtil = new PermissionUtil(this);
        //若没有权限
        if (!permissionUtil.hasPermissionGranted(PICTURE_PERMISSIONS)) {
            //请求所需权限
            permissionUtil.requestRequiredPermissions(PICTURE_PERMISSIONS, R.string.need_permissions, REQUEST_PICTURE_PERMISSION);
            return;
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

        } catch (InterruptedException e) {
            throw new RuntimeException("打开相机时中断");
        } catch (CameraAccessException e) {
            throw new RuntimeException("无法访问相机");
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/SmartCamera");
        if (!file.exists()) {
            file.mkdirs();
        }
        //照片保存路径
        mFile = new File(Environment.getExternalStorageDirectory().toString() + "/SmartCamera", FileUtil.getFileName(true));

        new File(Environment.getExternalStorageDirectory().toString());
        if (mCameraDevice == null) {
            return;
        }

        try {
            //创建作为拍照的CaptureRequest.Builder
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //将mImageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());

            //设置AF、AE模式
            setBuilder(captureRequestBuilder);

            //获得屏幕方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            characteristics = manager.getCameraCharacteristics(mCameraId);
            //获得Sensor方向
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            switch (mSensorOrientation) {
                //Sensor方向为90度时
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    //根据屏幕方向设置照片的方向
                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                //Sensor方向为270度时
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    //根据屏幕方向设置照片的方向
                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }

            //创建拍照的CameraCaptureSession.CaptureCallback对象
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                //在拍照完成时调用
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Toast.makeText(CameraActivity.this, "图片正在保存，请稍后......", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CameraActivity.this, "图片已保存！", Toast.LENGTH_SHORT).show();
                    updatePreview();//继续预览
                }
            };
            //停止连续取景
            mCameraCaptureSession.stopRepeating();
            //捕获静态图像
            mCameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //开启后台线程
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            //TextureView可用时，打开相机
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            //TextureView不可用时，为TextureView设置监听器
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
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
                            mButton.performClick();
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
            switch (seekBar.getId()) {
                case R.id.buchang:
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
                    }
                    break;
                case R.id.sewen:
                    int[] awb = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                    int wb = i / 11;
                    if (i == 9) {
                        wb = 8;
                    }
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, wb);
                    updatePreview();//更新预览
                    break;
                case R.id.sb_iso:
                    Range<Integer> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    if (range != null) {
                        int max1 = range.getUpper();//>=800
                        int min1 = range.getLower();//<=100
                        int iso = ((i * (max1 - min1)) / 100 + min1);
                        mPreviewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                        valueISO = iso;
                        tv_iso.setText("灵敏度：" + iso);
                        updatePreview();//更新预览
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
            switch (seekBar.getId()) {
                case R.id.sb_zoom:
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
                        }
                    } else if (seekTime2 % 3 == 1) {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }

                        //MF
                        Float range1 = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                        if (range1 != null) {
                            Float mf = Float.valueOf(i) / 100;
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                            mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mf);
                            zoom_text.setText(mf + "");
                            flag3 = false;
                        }
                    } else {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (timer_wb != null) {
                            timer_wb.cancel();
                        }
                        //WB CONTROL_AWB_MODE
                        int[] awb = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                        int wb = i / 11;
                        if (i == 9) {
                            wb = 8;
                        }
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, wb);
                        flag2 = false;
                    }
                    updatePreview();//更新预览
                    break;
                case R.id.sb_ev:

                    if (seekTime % 3 == 0) {
                        //EV
                        //曝光补偿
                        if (b) {
                            Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                            if (range1 != null) {
                                int maxmax = range1.getUpper();
                                int minmin = range1.getLower();
                                int all = (-minmin) + maxmax;
                                int time = 100 / all;
                                ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);

                                Log.e(TAG, "ae: " + CameraActivity.this.ae);
                                es_text.setText(CameraActivity.this.ae + "");
                            }
                        }
                    } else if (seekTime % 3 == 1) {
                        btn_auto.setAlpha(0.9f);
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (timer_type != null) {
                            timer_type.cancel();
                        }
                        //ES
                        //曝光时间
                        if (seekBar.getId() != R.id.buchang) {
                            Range<Long> rangeTime = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                            if (rangeTime != null) {
                                long max = rangeTime.getUpper();
                                long min = rangeTime.getLower();
                                long aet = ((i * (max - min)) / 100 + min);
                                mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, aet);
                                es_text.setText(aet + "");
                                flag1 = false;
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
                                es_text.setText("灵敏度：" + iso);
                                bansb_iso.setProgress(valueISO);
                                flag1 = false;
                            }
                        }
                    }

                    updatePreview();//更新预览
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
        // TODO Auto-generated method stub
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
            texture.setDefaultBufferSize(1920, 1080);
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
            mNextVideoAbsolutePath = "/storage/emulated/0/SmartCamera/VEDIO_" + time + ".mp4";
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

}
