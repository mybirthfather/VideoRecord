package com.huichao.video.record;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huichao.video.R;
import com.huichao.video.util.ActivityStartTool;
import com.zhy.autolayout.AutoLinearLayout;
import com.zhy.autolayout.AutoRelativeLayout;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ${爸爸} on 2017/1/6.
 */

public class BackCameraVideo extends AppCompatActivity implements TimeTask.OnDone {
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = 0;

    private static final String TAG = BackCameraVideo.class.getSimpleName();
    private static final int FOCUS_AREA_SIZE = 500;
    private Camera mCamera;
    private BackCameraPreview mPreview;
    private MediaRecorder mediaRecorder;
    private String url_file;
    private static boolean cameraFront = false;
    private static boolean flash = false;
    private long countUp;
    private int quality = CamcorderProfile.QUALITY_480P;

    private LinearLayout mCamreraPreview;
    private View mChangeCamera;
    private static final int RECORD=1000;
    private RoundProgressBar progress;
    private AutoRelativeLayout mXuanze,mCancel,mConfirm;
    private AutoLinearLayout mPaishe;
    private ImageView mBack;
    private long  noewTime=0;
    private boolean hasAudio=false;
    private int mCoverHeight;
    private int mPreviewHeight;
    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;
    View btnCoverView,topCoverView;
    private TextView mPressAndTime;

    public static void startActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CameraActivity.class);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_backvideo);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hasAudio=isHasPermission(this);
    }

    /**
     * //这个用来判断 是否有录音权限 否则直接崩 当前活动直接开始录音动作
     *如果上个界面 给权限了 我们直接下面的动作 开始录音 否则 会在
     *prepareMediaRecorder()初始化 {mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); 的这一行 直接报错  所以后面我把异常抓住了
     *实际你跑这个demo  他应该不会崩 会有提示 但我都注掉了
     *
     * @param context
     * @return
     */
    public static boolean isHasPermission(final Context context){
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //开始录制音频
        try{
            audioRecord.startRecording();
        }catch (IllegalStateException e){
            e.printStackTrace();
            return  false;
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (audioRecord.getRecordingState()!= AudioRecord.RECORDSTATE_RECORDING){
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        return true;
    }
    /**
     * 找前置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findFrontFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    /**
     * 找后置摄像头,没有则返回-1
     *
     * @return cameraId
     */
    private int findBackFacingCamera() {
        int cameraId = -1;
        //获取摄像头个数
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
    }


    public void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        MPermissions.requestPermissions(this,RECORD, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @PermissionGrant(RECORD)
    public void requestSdcardSuccess() {

        if (!hasCamera(getApplicationContext())) {
            //这台设备没有发现摄像头
            Toast.makeText(getApplicationContext(), R.string.dont_have_camera_error
                    , Toast.LENGTH_SHORT).show();
//            setResult(MainActivity.RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
            releaseCamera();
            releaseMediaRecorder();
            finish();
        }
        if (mCamera == null) {
            releaseCamera();
            final boolean frontal = cameraFront;

            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                //前置摄像头不存在
                switchCameraListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BackCameraVideo.this, R.string.dont_have_front_camera, Toast.LENGTH_SHORT).show();
                    }
                };

                //尝试寻找后置摄像头
                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
                }
            } else if (!frontal) {
                cameraId = findBackFacingCamera();
                if (flash) {
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
                }
            }
            try {
                mCamera = Camera.open(cameraId);
                initialize();
                reloadQualities(cameraId);
            }catch (Exception ex){
                Log.i("====",""+ex);
//                ToastContinuUtil.showToastTime(this,"请到设置允许设置相机权限",1000);
                finish();
            }

        }

    }

    @PermissionDenied(RECORD)
    public void requestSdcardFailed() {
        releaseCamera();
        releaseMediaRecorder();
        finish();
    }
    private boolean allSend=false;
    private TimeTask timeTask;
    //点击对焦
    public void initialize() {
        mPressAndTime = (TextView) findViewById(R.id.pressTv);
        mXuanze = (AutoRelativeLayout) findViewById(R.id.video_Rl_Xuanze);
        mCancel = (AutoRelativeLayout)findViewById(R.id.video_cancel_rl);
        mConfirm = (AutoRelativeLayout)findViewById(R.id.video_confirm_rl);
        mPaishe = (AutoLinearLayout) findViewById(R.id.video_paishe_Rl);
        mBack = (ImageView) findViewById(R.id.back_Iv);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaRecorder();
                releaseCamera();
                finish();
                overridePendingTransition(0,R.anim.close);

            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording = false;
                File mp4 = new File(url_file);//删除旧文件 并且释放相机之类的资源
                if (mp4.exists() && mp4.isFile()) {
                    mp4.delete();
                }
                releaseMediaRecorder();
//                releaseCamera();//这个地方不能释放向机 否则再回来就不能录像了 会报控
                mXuanze.setVisibility(View.GONE);
                mPaishe.setVisibility(View.VISIBLE);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaRecorder();
                recording = false;
                releaseCamera();
                    ActivityStartTool.actionStartStringFinish(BackCameraVideo.this, VideoPlay.class,"videopath",url_file);
                    Log.i("==========","url_file="+url_file);
                finish();
            }
        });
        progress = (RoundProgressBar) findViewById(R.id.button_capture);
        final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
//        mPreview = new BackCameraPreview(BackCameraVideo.this, mCamera);
         btnCoverView = findViewById(R.id.cover_bottom_view);
         topCoverView = findViewById(R.id.cover_top_view);
        mPreview= (BackCameraPreview) findViewById(R.id.camera_preview);
        mPreview.setCamera(mCamera);
        setupCamera();

        mPreview.refreshCamera(mCamera);
        if (mCoverHeight == 0) {
            ViewTreeObserver observer = mPreview.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = mPreview.getWidth();
                    mPreviewHeight = mPreview.getHeight();
                    mCoverHeight = Math.abs(mPreviewHeight - width) / 2;
                    Log.d(TAG+"相机", "preview width " + width + " height " + mPreviewHeight);

//                    Log.d(TAG+"相机", "getScreenDispaly width " + AppUtil.getScreenDispaly(BackCameraVideo.this)[0] + "getScreenDisplay height " + AppUtil.getScreenDispaly(BackCameraVideo.this)[1]);
                    Log.i(TAG,"首次mCoverHeight="+mCoverHeight);
                    topCoverView.getLayoutParams().height = mCoverHeight;
                    btnCoverView.getLayoutParams().height = mCoverHeight;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            topCoverView.getLayoutParams().height = mCoverHeight;
            btnCoverView.getLayoutParams().height = mCoverHeight;
        }
        timeTask = new TimeTask(20000, 1,progress);
        timeTask.setOnDone(this);
//        mCamreraPreview.addView(mPreview);
        progress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("========","setOnLongClickListener");
                return true;
            }
        });
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("========","setOnClickListener");
            }
        });
        progress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        progress.setTextColor(Color.RED);
                        //准备开始录制视频
                        if (!prepareMediaRecorder()) {
                            Toast.makeText(BackCameraVideo.this, getString(R.string.camera_init_fail), Toast.LENGTH_SHORT).show();
//                                setResult(MainActivity.RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
                            Log.i("========","初始化失败退出");
                            releaseCamera();
                            releaseMediaRecorder();
                            finish();
                        }
                        try {
                            if (hasAudio){
                                mPressAndTime.setText("按住拍摄");
                                mediaRecorder.start();//按下开始录制视频  对应抬手就得停止录制
                                timeTask.start();
                                recording=true;
//                            startChronometer();
                                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                } else {
                                    changeRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                }
//                            mBinding.buttonCapture.setImageResource(R.mipmap.player_stop);
                            }
                            else {
//                                ToastContinuUtil.showToastTime(BackCameraVideo.this,"无法获取音频权限",500);
                                releaseCamera();
                                releaseMediaRecorder();
                                finish();
                            }
                        }
                        catch (final Exception ex) {
//                            ToastContinuUtil.showToastTime(BackCameraVideo.this,"请去设置开启音频权限",400); //这个地方  吐司我都没做 正常得加
                            releaseCamera(); //如果没音频权限 6.0以下的手机会直接崩 即便你在清单文件声明 也没用 6.0以下小米 魅族 貌似有一套自己的权限管理
                            releaseMediaRecorder(); //这个地方捕获异常  并直接finish 用户是看不到的 他只能看到提示他去开权限
                            finish();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i(TAG,"mCoverHeight="+mCoverHeight);
                        topCoverView.getLayoutParams().height = mCoverHeight;
                        btnCoverView.getLayoutParams().height = mCoverHeight;
//                        Log.i(TAG,"topCoverView.getLayoutParams().height="+topCoverView.getLayoutParams().height);
                        Log.i(TAG,"btnCoverView.getLayoutParams().height="+btnCoverView.getLayoutParams().height);

                        if (hasAudio){
                            mPressAndTime.setText("按住拍摄");
                            progress.setProgress(0);
                            progress.setTextColor(Color.WHITE);
                            //这个地方 有个问题 就是本来一开始计划 只有20000-noewTime<1000这个条件的 但是 你会发现 如果你瞬间按下 在抬手 app就崩了..
                            //打log会发现  noewTime (其实应该是 newTime 靠) noewTime 竟然是0....这个也改了好久
                            if (20000-noewTime<1000||noewTime==0){//小于1000 即1s 啥也不做  进度条不走 并且 取消录制
                                noewTime=0;
                                if (recording){
//                                mediaRecorder.stop(); //停止
                                    releaseMediaRecorder();
                                    recording=false;
                                }
//                                ToastContinuUtil.showToastTime(BackCameraVideo.this,"录制时间不能少于一秒",500);
                                timeTask.cancel();
                            }
                            else { //否走对视频做处理
                                mPressAndTime.setText("按住拍摄");
                                noewTime=0;
                                timeTask.cancel();
                                stopRecordForVideo();
                            }
                        }

                        break;
                }
                return true;
            }
        });
        mChangeCamera = findViewById(R.id.button_ChangeCamera);
        mChangeCamera.setOnClickListener(switchCameraListener);
        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        focusOnTouch(event);
                    } catch (Exception e) {
                        Log.i(TAG, getString(R.string.fail_when_camera_try_autofocus, e.toString()));
                        //do nothing
                    }
                }
                return true;
            }
        });

    }
    private  Camera.Size bestPreviewSize;
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        bestPreviewSize = determineBestPreviewSize(parameters);
        Camera.Size bestPictureSize = determineBestPictureSize(parameters);
        Log.i(TAG,"bestPreviewSize.width"+bestPreviewSize.width+"bestPreviewSize.height"+bestPreviewSize.height);
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);


        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        // Lock in the changes
        mCamera.setParameters(parameters);
    }
    private Camera.Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Camera.Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }
    private Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold) {
        Camera.Size bestSize = null;
        Camera.Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            Log.i(TAG,"size.width="+size.width+"size.height="+size.height);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }
    private void stopRecordForVideo(){
        if (recording) {
            progress.setProgress(0);
            recording=false;
            //如果正在录制点击这个按钮表示录制完成
//                mediaRecorder.stop(); //停止
            releaseMediaRecorder();
            mPaishe.setVisibility(View.GONE);//录制按钮不可见
            mXuanze.setVisibility(View.VISIBLE);}
    }



    //reload成像质量
    private void reloadQualities(int idCamera) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);

        quality = prefs.getInt("QUALITY", CamcorderProfile.QUALITY_HIGH);
        changeVideoQuality(quality);

        final ArrayList<String> list = new ArrayList<String>();

        int maxQualitySupported = CamcorderProfile.QUALITY_480P;

        if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_480P)) {
            list.add("480p");
            maxQualitySupported = CamcorderProfile.QUALITY_480P;
        }
        if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_720P)) {
            list.add("720p");
            maxQualitySupported = CamcorderProfile.QUALITY_720P;
        }
        if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_1080P)) {
            list.add("1080p");
            maxQualitySupported = CamcorderProfile.QUALITY_1080P;
        }
        if (CamcorderProfile.hasProfile(idCamera, CamcorderProfile.QUALITY_2160P)) {
            list.add("2160p");
            maxQualitySupported = CamcorderProfile.QUALITY_2160P;
        }

        if (!CamcorderProfile.hasProfile(idCamera, quality)) {
            quality = maxQualitySupported;
        }
    }
    //闪光灯
//    View.OnClickListener flashListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (!recording && !cameraFront) {
//                if (flash) {
//                    flash = false;
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_off_white);
//                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                } else {
//                    flash = true;
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_on_white);
//                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                }
//            }
//        }
//    };

    //切换前置后置摄像头
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    releaseCamera();
                    chooseCamera();
                } else {
                    //只有一个摄像头不允许切换
                    Toast.makeText(getApplicationContext(), R.string.only_have_one_camera
                            , Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    //选择摄像头
    public void chooseCamera() {
        if (cameraFront) {
            //当前是前置摄像头
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                try {
                    mCamera = Camera.open(cameraId);
                    // mPicture = getPictureCallback();
                    mPreview.refreshCamera(mCamera);
                    setupCamera();
                }catch (Exception ex){

                }

                reloadQualities(cameraId);
            }
        } else {
            //当前为后置摄像头
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                try {
                    mCamera = Camera.open(cameraId);
                    mPreview.refreshCamera(mCamera);
                    setupCamera();//这个方法设置不变形 切换相机 必须调用  否则还是变形的
                    reloadQualities(cameraId);
                }catch (Exception ex){

                }

                if (flash) {
                    flash = false;
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_off_white);
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                // mPicture = getPictureCallback();

            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"pause");
        if (recording){
            Log.i(TAG,"recording="+recording);
            releaseMediaRecorder();
            recording=false;
            if (timeTask!=null){
                timeTask.cancel();
            }
        }
//        releaseCamera();
    }

    //检查设备是否有摄像头
    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    boolean recording = false;
    private void changeRequestedOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            if (mCamera!=null){
                mCamera.lock();
            }
        }
    }
    private boolean prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        try {
            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//这个地方会报异常 如果拒绝权限的话
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOrientationHint(90);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (cameraFront) {
                    mediaRecorder.setOrientationHint(270);
                } else {
                    Log.i("=============","setOrientationHint90");
                    mediaRecorder.setOrientationHint(90);
                }
            }
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    /* Fixed video Size: 640 * 480*/

//        mediaRecorder.setVideoSize(AppUtil.getScreenDispaly(this)[1], AppUtil.getScreenDispaly(this)[0]);
//        Log.i("===========","AppUtil.getScreenDispaly(this)[1]="+AppUtil.getScreenDispaly(this)[1]+"AppUtil.getScreenDispaly(this)[0=]"+AppUtil.getScreenDispaly(this)[0]);
    /* Encoding bit rate: 1 * 1024 * 1024*/
            mediaRecorder.setVideoSize(1280, 720);//设置为这个 也只是部分手机全屏 不知道别的手机行不行
//        mediaRecorder.setVideoFrameRate(20);
            mediaRecorder.setVideoEncodingBitRate( 2*1024 * 1024);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mediaRecorder.setProfile(CamcorderProfile.get(quality));
            String path= Environment.getExternalStorageDirectory().getPath() + "/beauty/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            Date d = new Date();
            String timestamp = String.valueOf(d.getTime());
            url_file = path + timestamp + ".mp4";
//        url_file = "/mnt/sdcard/videokit/" + timestamp + ".mp4";
            File file1 = new File(url_file);
            if (file1.exists()) {
                file1.delete();
            }
            mediaRecorder.setOutputFile(url_file);
        }catch (Exception ex){
//            ToastContinuUtil.showToastTime(this,"请去设置打开音频权限",500);
            return false;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    //修改录像质量
    private void changeVideoQuality(int quality) {
        SharedPreferences prefs = getSharedPreferences("RECORDING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("QUALITY", quality);
        editor.commit();
        this.quality = quality;
//        updateButtonText(quality);
    }

    private void updateButtonText(int quality) {
//        if (quality == CamcorderProfile.QUALITY_480P)
//            mBinding.buttonQuality.setText("480p");
//        if (quality == CamcorderProfile.QUALITY_720P)
//            mBinding.buttonQuality.setText("720p");
//        if (quality == CamcorderProfile.QUALITY_1080P)
//            mBinding.buttonQuality.setText("1080p");
//        if (quality == CamcorderProfile.QUALITY_2160P)
//            mBinding.buttonQuality.setText("2160p");
    }

    @Override
    public void onCount(long millisUntilFinished) {//传过来当前倒计时的值
        noewTime=millisUntilFinished;
        mPressAndTime.setText("00:"+getTime(noewTime));
    }
    private String getTime(long time){
        long result=  (20000-noewTime)/1000;
        if (result>=10){
            return String.valueOf(result);
        }
        else {
            return 0+ String.valueOf(result);
        }

    }
    @Override
    public void onCountFinish() {//倒计时完成
        progress.setProgress(0);
        stopRecordForVideo();
        noewTime=0;
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    //闪光灯
    public void setFlashMode(String mode) {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)
                    && mCamera != null
                    && !cameraFront) {

                mPreview.setFlashMode(mode);
                mPreview.refreshCamera(mCamera);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.changing_flashLight_mode,
                    Toast.LENGTH_SHORT).show();
        }
    }

    //计时器
    private void startChronometer() {
//        mBinding.textChrono.setVisibility(View.VISIBLE);
        final long startTime = SystemClock.elapsedRealtime();
//        mBinding.textChrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
//            @Override
//            public void onChronometerTick(Chronometer arg0) {
//                countUp = (SystemClock.elapsedRealtime() - startTime) / 1000;
//                if (countUp % 2 == 0) {
//                    mBinding.chronoRecordingImage.setVisibility(View.VISIBLE);
//                } else {
//                    mBinding.chronoRecordingImage.setVisibility(View.INVISIBLE);
//                }
//
//                String asText = String.format("%02d", countUp / 60) + ":" + String.format("%02d", countUp % 60);
////                mBinding.textChrono.setText(asText);
//            }
//        });
//        mBinding.textChrono.start();
    }

    private void stopChronometer() {
//        mBinding.textChrono.stop();
//        mBinding.chronoRecordingImage.setVisibility(View.INVISIBLE);
//        mBinding.textChrono.setVisibility(View.INVISIBLE);
    }

    public static void reset() {
        flash = false;
        cameraFront = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("=====","event="+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                Log.i("======","KeyEvent.KEYCODE_BACK");
                if (recording) {
                    mediaRecorder.stop();
//            if (mBinding.textChrono != null && mBinding.textChrono.isActivated())
//                mBinding.textChrono.stop();
                    releaseMediaRecorder();
                    recording = false;
                    File mp4 = new File(url_file);
                    if (mp4.exists() && mp4.isFile()) {
                        mp4.delete();
                    }
                }
                releaseCamera();
                releaseMediaRecorder();
                finish();
                overridePendingTransition(0,R.anim.close);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN: //必须重写 否则录像的时候 按音量键 会退到桌面 电源键 不影响惠锁屏 但是还能回来
                Log.i("====","KEYCODE_VOLUME_DOWN");
                return  false;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.i("====","KEYCODE_VOLUME_UP");
                return false;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                Rect rect = calculateFocusArea(event.getX(), event.getY());
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            } else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }
    private String getUrl_file(){
        String path = Environment.getExternalStorageDirectory().getPath();
        path+="in.mp4";
        return path;
    }
    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus", "success!");
            } else {
                // do something...
                Log.i("tap_to_focus", "fail!");
            }
        }
    };
}
