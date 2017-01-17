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
 * Created by shuyu on 2016/12/7.
 */

public class SampleVideo extends StandardGSYVideoPlayer {

    private TextView mMoreScale;

    private TextView mSwitchSize;



    //记住切换数据源类型
    private int mType = 0;

    //数据源
    private int mSourcePosition = 0;
    private ImageView mStartPause;
    private ImageView mMiddleStart;
    private LinearLayout mOutStartPause;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public SampleVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleVideo(Context context) {
        super(context);
    }

    public SampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }
    private void initView() {

        mStartPause = (ImageView) findViewById(R.id.startpausebottom);
        mMiddleStart = (ImageView) findViewById(R.id.start);
        mOutStartPause = (LinearLayout) findViewById(R.id.outStartPause);
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
//                        mStartPause.setImageResource(R.drawable.nav_kaishi);
                        mStartPause.setImageResource(R.mipmap.nav_guanbi_w);
                        startButtonLogic();
                    }
                }




//                if (mCurrentState==CURRENT_STATE_PLAYING){
//                    mStartPause.setImageDrawable(getResources().getDrawable(R.drawable.nav_guanbi));
//                    GSYVideoManager.instance().getMediaPlayer().pause();
//                    mCurrentState=CURRENT_STATE_PAUSE;
//                }else {
//                    mStartPause.setImageDrawable(getResources().getDrawable(R.drawable.nav_kaishi));
//                    GSYVideoManager.instance().getMediaPlayer().start();
//                    mCurrentState=CURRENT_STATE_PLAYING;
////                    startPlayLogic();
//                }


            }
        });

        //切换清晰度


        //切换视频清晰度

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


//    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, Object... objects) {
//        mUrlList = url;
//        return setUp(url.get(0).getUrl(), cacheWithPlay, objects);
//    }


//    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, File cachePath, Object... objects) {
//        mUrlList = url;
//        return setUp(url.get(0).getUrl(), cacheWithPlay, cachePath, objects);
//    }

    @Override
    public int getLayoutId() {
//        if (mIfCurrentIsFullscreen) {
//            return R.layout.sample_video_land;
//        }
        return R.layout.sample_video;
//        return R.layout.sample_video;
    }

    /**
     * 弹出切换清晰度
     */
//    private void showSwitchDialog() {
//        SwitchVideoTypeDialog switchVideoTypeDialog = new SwitchVideoTypeDialog(getContext());
//        switchVideoTypeDialog.initList(mUrlList, new SwitchVideoTypeDialog.OnListItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                final String name = mUrlList.get(position).getName();
//                if (mSourcePosition != position) {
//                    if ((mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING
//                            || mCurrentState == GSYVideoPlayer.CURRENT_STATE_PAUSE)
//                            && GSYVideoManager.instance().getMediaPlayer() != null) {
//                        final String url = mUrlList.get(position).getUrl();
//                        onVideoPause();
//                        final long currentPosition = mCurrentPosition;
//                        GSYVideoManager.instance().releaseMediaPlayer();
//                        cancelProgressTimer();
//                        hideAllWidget();
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                setUp(url, mCache, mCachePath, mObjects);
//                                setSeekOnStart(currentPosition);
//                                startPlayLogic();
//                                cancelProgressTimer();
//                                hideAllWidget();
//                            }
//                        }, 500);
//                        mSwitchSize.setText(name);
//                        mSourcePosition = position;
//                    }
//                } else {
//                    Toast.makeText(getContext(), "已经是 " + name, Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//        switchVideoTypeDialog.show();
//    }


//    @Override
//    protected void setStateAndUi(int state) {
//        super.setStateAndUi(state);
//        switch(this.mCurrentState) {
//            case 6:
//                mMiddleStart.setVisibility(INVISIBLE);
//                this.mBottomProgressBar.setProgress(0);
////                this.changeUiToCompleteShow();
////                this.cancelDismissControlViewTimer();
////                this.mBottomProgressBar.setProgress(100);
//                break;
//        }
//    }
}
