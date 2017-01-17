/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
 * 
 * 由于Camera在SDK 21的版本被标为Deprecated,建议使用新的Camera2类来实现
 * 但是由于Camera2这个类要求minSDK大于21,所以依旧使用Camera这个类进行实现 此视频录制最长为19S
 */
public class CameraActivity extends AppCompatActivity implements TimeTask.OnDone {
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
    private static final String TAG = CameraActivity.class.getSimpleName();
    private static final int FOCUS_AREA_SIZE = 500;
    private Camera mCamera;
    private CameraPreview mPreview;
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
    private Intent mIntent;
    private int mTag;
    private boolean hasAudio=false;
    private int  limit=0;
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
        setContentView(R.layout.activity_camera);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mIntent = getIntent();
        mTag = mIntent.getIntExtra("tag",-1);
       if (mTag==1){
            limit=5000;
        }
        else {
           limit=1000;
       }
        hasAudio=isHasPermission(this);
    }
    public static boolean isHasPermission(final Context context){//这个用来判断 是否有录音权限 否则直接崩
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

    public void onResume() {
        super.onResume();
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
                        Toast.makeText(CameraActivity.this, R.string.dont_have_front_camera, Toast.LENGTH_SHORT).show();
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
//                ToastContinuUtil.showToastTime(this,"请到设置中开启相机权限",500);
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
//                releaseCamera();//这个地方不能是方向机 否则再回来就不能录像了 会报控
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
                if (mTag==0){
                    Intent intent = new Intent();
                    intent.putExtra("path",url_file);
                    setResult(RESULT_OK,intent);
                }else if (mTag==1){
                    Intent intent = new Intent();
                    intent.putExtra("path",url_file);
                    setResult(RESULT_OK,intent);
                }
                else {//默认的
                    ActivityStartTool.actionStartStringFinish(CameraActivity.this, VideoPlay.class,"videopath",url_file);
                    Log.i("==========","url_file="+url_file);
                }
                finish();
            }
        });
        progress = (RoundProgressBar) findViewById(R.id.button_capture);
        final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        mPreview = new CameraPreview(CameraActivity.this, mCamera);
        mPreview.refreshCamera(mCamera);
        mCamreraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        timeTask = new TimeTask(20000, 1,progress);
        timeTask.setOnDone(this);
        mCamreraPreview.addView(mPreview);
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
                                Toast.makeText(CameraActivity.this, getString(R.string.camera_init_fail), Toast.LENGTH_SHORT).show();
//                                setResult(MainActivity.RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
                                Log.i("========","初始化失败退出");
                                releaseCamera();
                                releaseMediaRecorder();
                                finish();
                            }
                        try {
                            if (hasAudio){
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
//                                ToastContinuUtil.showToastTime(CameraActivity.this,"请到设置中开启音频权限",500);
                                releaseCamera();
                                releaseMediaRecorder();
                                finish();
                            }
                        }
                         catch (final Exception ex) {
//                            ToastContinuUtil.showToastTime(CameraActivity.this,"请到设置中开启音频权限",400);
//                            setResult(MainActivity.RESULT_CODE_FOR_RECORD_VIDEO_FAILED);
                            releaseCamera();
                            releaseMediaRecorder();
                            finish();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("==============","limit="+limit);
                        if (hasAudio){
                            mPressAndTime.setText("按住拍摄");
                            progress.setProgress(0);
                            progress.setTextColor(Color.WHITE);
                            if (20000-noewTime<limit||noewTime==0){//小于1000 即1s 啥也不做  进度条不走 并且 取消录制
                                noewTime=0;
                                if (recording){
//                                mediaRecorder.stop(); //停止
                                    releaseMediaRecorder();
                                    recording=false;
                                }
//                                ToastContinuUtil.showToastTime(CameraActivity.this,"录制时间不能少于"+limit/1000+"秒",500);
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
//        mBinding.buttonQuality.setOnClickListener(qualityListener);
//        mBinding.buttonFlash.setOnClickListener(flashListener);
        mCamreraPreview.setOnTouchListener(new View.OnTouchListener() {
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
//            updateButtonText(maxQualitySupported);
        }

//        final StableArrayAdapter adapter = new StableArrayAdapter(this,
//                android.R.layout.simple_list_item_1, list);
//        mBinding.listOfQualities.setAdapter(adapter);

//        mBinding.listOfQualities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view,
//                                    int position, long id) {
//                final String item = (String) parent.getItemAtPosition(position);
//
//                mBinding.buttonQuality.setText(item);
//
//                if (item.equals("480p")) {
//                    changeVideoQuality(CamcorderProfile.QUALITY_480P);
//                } else if (item.equals("720p")) {
//                    changeVideoQuality(CamcorderProfile.QUALITY_720P);
//                } else if (item.equals("1080p")) {
//                    changeVideoQuality(CamcorderProfile.QUALITY_1080P);
//                } else if (item.equals("2160p")) {
//                    changeVideoQuality(CamcorderProfile.QUALITY_2160P);
//                }
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    mBinding.listOfQualities.animate().setDuration(200).alpha(0)
//                            .withEndAction(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mBinding.listOfQualities.setVisibility(View.GONE);
//                                }
//                            });
//                } else {
//                    mBinding.listOfQualities.setVisibility(View.GONE);
//                }
//            }
//
//        });

    }

//    View.OnClickListener qualityListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (!recording) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
//                        && mBinding.listOfQualities.getVisibility() == View.GONE) {
//                    mBinding.listOfQualities.setVisibility(View.VISIBLE);
//                    mBinding.listOfQualities.animate().setDuration(200).alpha(95)
//                            .withEndAction(new Runnable() {
//                                @Override
//                                public void run() {
//                                }
//
//                            });
//                } else {
//                    mBinding.listOfQualities.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    };

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
                mCamera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
//                reloadQualities(cameraId);
            }
        } else {
            //当前为后置摄像头
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview
                mCamera = Camera.open(cameraId);
                if (flash) {
                    flash = false;
//                    mBinding.buttonFlash.setImageResource(R.mipmap.ic_flash_off_white);
                    mPreview.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                // mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
                reloadQualities(cameraId);
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
            mediaRecorder.setVideoSize(1280, 720);//设置为这个 就全屏了 而且 设置帧率为20 就很清晰了
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
//            ToastContinuUtil.showToastTime(this,"请到设置中开启音频权限",500);
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
            case KeyEvent.KEYCODE_VOLUME_DOWN:
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

