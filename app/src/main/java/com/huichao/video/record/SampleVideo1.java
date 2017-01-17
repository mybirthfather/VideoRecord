package com.huichao.video.record;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huichao.video.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * Created by Administrator on 2017/1/4.
 */

public class SampleVideo1 extends StandardGSYVideoPlayer {

    private TextView mMoreScale;

    private TextView mSwitchSize;



    //记住切换数据源类型
    private int mType = 0;

    //数据源
    private int mSourcePosition = 0;
    private ImageView mStartPause;
    private ImageView mMiddleStart;
    private LinearLayout mOutStartPause;
    private View thumb,layout_bottom;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public SampleVideo1(Context context, Boolean fullFlag) {
        super(context, fullFlag);
        initView();
    }

    public SampleVideo1(Context context) {
        super(context);
        initView();
    }
    public SampleVideo1(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    protected void init(Context context) {

        super.init(context);
        initView();
    }
    private void initView() {
        layout_bottom = findViewById(R.id.layout_bottom);
        mStartPause = (ImageView) findViewById(R.id.startpausebottom);
        mMiddleStart = (ImageView) findViewById(R.id.start);
        mOutStartPause = (LinearLayout) findViewById(R.id.outStartPause);

        thumb = findViewById(R.id.thumb);
        setBottomShowProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress),getResources().getDrawable(R.drawable.video_new_seekbar_thumb));//前面是进度 后面是背景
      setBottomProgressBarDrawable(getResources().getDrawable(R.drawable.video_new_seekbar_progress));//底部的进度条 可以省略
        thumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setOnOutView.onOutView();
            }
        });
        mOutStartPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.outStartPause) {
                    if (TextUtils.isEmpty(mUrl)) {
                        Toast.makeText(getContext(), getResources().getString(com.shuyu.gsyvideoplayer.R.string.no_url), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR) {
                        if (!mUrl.startsWith("file") && !CommonUtil.isWifiConnected(getContext())
                                && mNeedShowWifiTip) {
                            showWifiDialog();
                            return;
                        }
                        Log.i("========","进来点击");
                        startButtonLogic();
                    } else if (mCurrentState == CURRENT_STATE_PLAYING) {
                        GSYVideoManager.instance().getMediaPlayer().pause();
                        Log.i("========","CURRENT_STATE_PLAYING");
                        setStateAndUi(CURRENT_STATE_PAUSE);
                        mStartPause.setImageResource(R.mipmap.nav_kaishi_w);
                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                            if (mIfCurrentIsFullscreen) {
                                Debuger.printfLog("onClickStopFullscreen");
                                mVideoAllCallBack.onClickStopFullscreen(mUrl, mObjects);
                            } else {
                                Debuger.printfLog("onClickStop");
                                mVideoAllCallBack.onClickStop(mUrl, mObjects);
                            }
                        }
                    } else if (mCurrentState == CURRENT_STATE_PAUSE) {
                        Log.i("========","CURRENT_STATE_PAUSE");
                        if (mVideoAllCallBack != null && isCurrentMediaListener()) {
                            if (mIfCurrentIsFullscreen) {
                                Debuger.printfLog("onClickResumeFullscreen");
                                mVideoAllCallBack.onClickResumeFullscreen(mUrl, mObjects);
                            } else {
                                Debuger.printfLog("onClickResume");
                                mVideoAllCallBack.onClickResume(mUrl, mObjects);
                            }
                        }
                        GSYVideoManager.instance().getMediaPlayer().start();
                        mStartPause.setImageResource(R.mipmap.nav_guanbi_w);
                        setStateAndUi(CURRENT_STATE_PLAYING);
                    } else if (mCurrentState == CURRENT_STATE_AUTO_COMPLETE) {
                        Log.i("========","CURRENT_STATE_AUTO_COMPLETE");
                        mStartPause.setImageResource(R.mipmap.nav_guanbi_w);
                        startButtonLogic();
                    }
                }
            }
        });




    }



    private void startButtonLogic() {
        if(this.mVideoAllCallBack != null && this.mCurrentState == 0) {
            Debuger.printfLog("onClickStartIcon");
//            mStartPause.setImageResource(R.drawable.nav_guanbi);
            this.mVideoAllCallBack.onClickStartIcon(this.mUrl, this.mObjects);
            Log.i("============","mCurrentState=0+"+mCurrentState);
        } else if(this.mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartError");
            Log.i("============","mCurrentState+"+mCurrentState);
//            mStartPause.setImageResource(R.drawable.nav_kaishi);
            this.mVideoAllCallBack.onClickStartError(this.mUrl, this.mObjects);
        }
        this.prepareVideo();
    }
    @Override
    protected void showPauseCover() {

    }
    @Override
    protected void updateStartImage() {
//        ImageView imageView = (ImageView) mStartButton;
        if (mCurrentState == CURRENT_STATE_PLAYING) {
            mMiddleStart.setImageResource(R.drawable.video_click_pause_selector);
            mStartPause.setImageResource(R.mipmap.nav_guanbi_w);
        } else if (mCurrentState == CURRENT_STATE_ERROR) {
            mStartPause.setImageResource(R.mipmap.nav_guanbi_w);
            mMiddleStart.setImageResource(R.drawable.video_click_play_selector);
        } else {
            mStartPause.setImageResource(R.mipmap.nav_kaishi_w);
            mMiddleStart.setImageResource(R.drawable.video_click_play_selector);
        }
    }

    @Override
    protected void setStateAndUi(int state) {
        super.setStateAndUi(state);
        switch (mCurrentState){
            case 6:
                layout_bottom.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video;
    }
    private SetOnOutView setOnOutView;
    public void setOnOutviewListenr(SetOnOutView setOnOutView){
        this.setOnOutView = setOnOutView;
    }



    public interface SetOnOutView{
        void onOutView();
    }




}

