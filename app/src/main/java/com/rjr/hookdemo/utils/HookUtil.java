package com.rjr.hookdemo.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
            // 反射拿到ActivityManagerNative的class文件
            Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");

            // 获取gDefault字段
            Field gDefaultField = amnClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);

            // 通过gDefault拿到该字段的值，即Singleton对象
            Object singleton = gDefaultField.get(null);

            // 拿到singleton里的mInstance属性
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            // 拿到mInstance的值，即IActivityManager对象
            // IActivityManager是真正调用startActivity的对象
            Object iActivityManagerImpl = mInstanceField.get(singleton);

            // 通过动态代理，拦截掉startActivity方法
            ProxyInvocationHandler handler = new ProxyInvocationHandler(iActivityManagerImpl);
            Class<?> iamClass = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(mContext.getApplicationContext().getClassLoader(), new Class[]{iamClass}, handler);

            // 把代理对象赋值给mInstance属性，实际上所有的方法都有我们的代理对象来调用
            mInstanceField.set(singleton, proxy);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "hookAms: ", e);
        }
    }

    class ProxyInvocationHandler implements InvocationHandler {

        private Object iActivityManager;

        public ProxyInvocationHandler(Object iActivityManager) {
            this.iActivityManager = iActivityManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i(TAG, "invoke: " + method.getName());
            return method.invoke(iActivityManager, args);
        }
    }
}
