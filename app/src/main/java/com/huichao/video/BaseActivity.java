package com.huichao.video;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/9/27.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public SharedPreferences sp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadXML();


        initView();
        initData();
        initlistener();

    }
    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }



    protected abstract void loadXML();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initlistener();

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setHandler(msg);
        }
    };

    public void setHandler(Message msg){

    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (null != requestCall) {
//            requestCall.cancel();
//            requestCall = null;
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacksAndMessages(null);
    }
}
