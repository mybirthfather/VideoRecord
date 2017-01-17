package com.huichao.video.record;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.huichao.video.BaseActivity;
import com.huichao.video.R;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ${爸爸} on 2016/12/27.
 */

public class VideoPlay extends BaseActivity {
    private String mVideopath;
    private SampleVideo mVideo;
    private OrientationUtils orientationUtils;
    private ExecutorService mExecutorService;

    @Override
    protected void loadXML() {
        setContentView(R.layout.actiivty_videoplay);
    }

    @Override
    protected void initView() {
        mVideo = (SampleVideo) findViewById(R.id.video);
    }

    @Override
    protected void initData() {

        Debuger.enable();
        Intent intent = getIntent();
//        String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        mVideopath = intent.getStringExtra("videopath");
        Log.i("============","mVideopath="+mVideopath);
        mVideo.setUp(mVideopath,true);
        mVideo.setIsTouchWiget(true);
                mExecutorService = Executors.newSingleThreadExecutor();
        mVideo.setBottomShowProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress),getResources().getDrawable(R.drawable.video_new_seekbar_thumb));//前面是进度 后面是背景
        mVideo.setBottomProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress));//底部的进度条 可以省略
//        orientationUtils = new OrientationUtils(this, mVideo);

        //设置全屏按键功能
//        mVideo.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                orientationUtils.resolveByClick();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mVideo.setUp(mVideopath,true);
        mVideo.setIsTouchWiget(true);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
//                final Bitmap videoThumbnail = VideoThumbnails.createVideoThumbnail(mVideopath, mVideo);
//                Log.i("========","videoThumbnail="+videoThumbnail);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ImageView imageView = new ImageView(VideoPlay.this);
//                        imageView.setImageBitmap(videoThumbnail);
//                        mVideo.setThumbImageView(imageView);
//                    }
//                });
            }
        });



    }

    @Override
    protected void initlistener() {
        mVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("===========","mVideo.setOnClickListener点击了");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i("==========",""+ AppUtil.getScreenDispaly(this)[0]+"[[[["+AppUtil.getScreenDispaly(this)[1]);
        ViewTreeObserver vto2 = mVideo.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = mVideo.getMeasuredHeight();
                int width = mVideo.getMeasuredWidth();
                Log.i("===========","mVideo.getWidth()="+width+"mVideo.getHeight()="+height);
                mVideo.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
//        mVideo.onVideoResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideo.onVideoPause();
    }
    private void get(String path){
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideo.release();
    }
}
