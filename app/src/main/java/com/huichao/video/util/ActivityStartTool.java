package com.huichao.video.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/27.
 */
public class ActivityStartTool {
    /*跳转浏览器*/
    public static void actionStartWeb(Context context, String shareLink){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(shareLink);
        intent.setData(content_url);
        context.startActivity(intent);
    }
    /*普通的跳转与跳转关闭*/
    public static void actionStart(Context context, Class<?> c){
        Intent intent=new Intent(context,c);
        context.startActivity(intent);
    }
    public static void actionStartFinish(Context context, Class<?> c){
        Activity activity= (Activity) context;
        Intent intent=new Intent(context,c);
        context.startActivity(intent);
        activity.finish();
    }
    /*跳转传值String类型的值与关闭*/
    public static void actionStartString(Context context, Class<?> c, String key, String value){
        Intent intent=new Intent(context,c);
        intent.putExtra(key,value);
        context.startActivity(intent);
    }
    public static void actionStartStringFinish(Context context, Class<?> c, String key, String value){
        Activity activity= (Activity) context;
        Intent intent=new Intent(context,c);
        intent.putExtra(key,value);
        context.startActivity(intent);
        activity.finish();
    }
    /*跳转传值int类型的值与关闭*/
    public static void actionStartInt(Context context, Class<?> calzz, String key, int data){
        Intent intent = new Intent(context,calzz);
        intent.putExtra(key, data);
        context.startActivity(intent);
    }
    /*跳转传值boolean类型的值并关闭*/
    public static void actionStartBolleanFinish(Context context, Class<?> calzz, String key, boolean data){
        Activity activity= (Activity) context;
        Intent intent = new Intent(context,calzz);
        intent.putExtra(key, data);
        context.startActivity(intent);
        activity.finish();
    }
    public static void actionStartIntFinish(Context context, Class<?> calzz, String key, int data){
        Activity activity= (Activity) context;
        Intent intent = new Intent(context,calzz);
        intent.putExtra(key, data);
        context.startActivity(intent);
        activity.finish();
    }
    /*跳转关闭中间的Activity*/
    public static void actionStartAndFinshAll(Context context, Class<?> calzz){
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity,calzz);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        activity.finish();
    }
    /*跳转 传值 关闭中间的Activity*/
    public static void actionStartAndFinshAllwhitData(Context context, Class<?> calzz, String key, int data){
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity,calzz);
        intent.putExtra(key, data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关闭中间的activity
        context.startActivity(intent);
        activity.finish();
    }
    public static void actionStartAndFinshAllwhitData1(Context context, Class<?> calzz, String key, boolean data){
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity,calzz);
        intent.putExtra(key, data);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关闭中间的activity
        context.startActivity(intent);
        activity.finish();
    }

    public static void actionStartAndwhitList(Context context, Class<?> calzz, String key, ArrayList<String> data, int postion){
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity,calzz);
        intent.putStringArrayListExtra(key,data);
        intent.putExtra("postion",postion);
        context.startActivity(intent);
    }




}
