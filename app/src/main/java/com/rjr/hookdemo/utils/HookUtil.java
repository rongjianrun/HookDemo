package com.rjr.hookdemo.utils;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2018/3/23.
 */

public class HookUtil {
    private static HookUtil ourInstance;
    private Context context;

    public static HookUtil getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new HookUtil(context);
        }
        return ourInstance;
    }

    private HookUtil(Context context) {
        this.context = context;
    }

    public void fun() {
        // 一路反射，直到拿到IActivityManager对象
        try {
            Class<?> amClass = Class.forName("android.app.ActivityManager");
            Field iamField = amClass.getField("IActivityManagerSingleton");
            iamField.setAccessible(true);
            Object iamObj = iamField.get(null);
            // 反射拿到IActivityManager对象
            Class<?> singleClass = Class.forName("android.util.Singleton");
            Field insField = singleClass.getField("mInstance");
            insField.setAccessible(true);
            Object o = insField.get(iamObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}










