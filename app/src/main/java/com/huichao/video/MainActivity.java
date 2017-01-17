package com.huichao.video;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.huichao.video.record.BackCameraVideo;
import com.huichao.video.record.CameraActivity;
import com.huichao.video.util.ActivityStartTool;
import com.zhy.m.permission.MPermissions;
import com.zhy.m.permission.PermissionDenied;
import com.zhy.m.permission.PermissionGrant;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD=1001;//这个随便给的
    private int  tag=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.big).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//在调到录像的界面 就申请权限
                tag=0;
                //这个是 申请权限的 很好用
                MPermissions.requestPermissions(MainActivity.this,RECORD, Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE);

//                ActivityStartTool.actionStart(MainActivity.this, CameraActivity.class);
            }
        });
        findViewById(R.id.small).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag=1;
                MPermissions.requestPermissions(MainActivity.this,RECORD, Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE);
//                ActivityStartTool.actionStart(MainActivity.this, BackCameraVideo.class);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @PermissionGrant(RECORD)
    public void requestSdcardSuccess() {
        if (tag==0){
            ActivityStartTool.actionStart(MainActivity.this, CameraActivity.class);//大视频
            overridePendingTransition(R.anim.open,R.anim.stay);//这个是个动画 实际项目可以去掉
        }
        else if (tag==1){
            ActivityStartTool.actionStart(MainActivity.this, BackCameraVideo.class);//小视频
            overridePendingTransition(R.anim.open,R.anim.stay);
        }
       tag=-1;//做完必须复位 不然下次会走反的
    }

    @PermissionDenied(RECORD)
    public void requestSdcardFailed() {
        tag=-1;
        Toast.makeText(this,"不给权限啥也做不了",Toast.LENGTH_SHORT).show();
    }



}
