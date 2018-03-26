package com.rjr.hookdemo.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2018/3/26.
 */

public class HookUtil {

    private static final String TAG = "rong";
    private static HookUtil instance;

    private Context mContext;

    public static HookUtil getInstance(Context context) {
        if (instance == null) {
            instance = new HookUtil(context);
        }
        return instance;
    }

    private HookUtil(Context context) {
        mContext = context;
    }

    public void hookAms() {
        try {
            Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = amnClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            // 反射拿到singleton对象
            Object singletonObj = gDefaultField.get(null);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field iamField = singletonClass.getDeclaredField("mInstance");
            iamField.setAccessible(true);
            // 反射拿到IActivityManager对象，真正调用startActivity的对象
            Object iActivityManager = iamField.get(singletonObj);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "hookAms: ", e);
        }
    }
}
