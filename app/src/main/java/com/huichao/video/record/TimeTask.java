package com.huichao.video.record;

import android.os.CountDownTimer;

/**
 * Created by ${爸爸} on 2016/12/26.
 */

public class TimeTask extends CountDownTimer {

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    private RoundProgressBar mRoundProgressBar;
    public TimeTask(long millisInFuture, long countDownInterval,RoundProgressBar progressBar) {
        super(millisInFuture, countDownInterval);
        this.mRoundProgressBar=progressBar;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        mOnDone.onCount(millisUntilFinished);
        mRoundProgressBar.setProgress((int)(20000-millisUntilFinished));


    }

    @Override
    public void onFinish() {
        mRoundProgressBar.setProgress(10000);
        mOnDone.onCountFinish();
    }
    private OnDone mOnDone;

    public void setOnDone(OnDone onDone) {
        mOnDone = onDone;
    }

    public interface OnDone{
        void onCount(long millisUntilFinished);
        void onCountFinish();

    }
}
