package com.rjr.hookdemo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rjr.hookdemo.LoginActivity;
import com.rjr.hookdemo.ProxyActivity;

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
            if ("startActivity".equals(method.getName())) {
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        intent = (Intent) args[i];
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                // 设置代理的intent
                proxyIntent.setComponent(new ComponentName(mContext, ProxyActivity.class));
                // 真正要启动的intent
                proxyIntent.putExtra("oldIntent", intent);
                args[index] = proxyIntent;
            }
            return method.invoke(iActivityManager, args);
        }
    }

    public void hookSysHandler() {
        try {
            Class<?> atClass = Class.forName("android.app.ActivityThread");
            Field atField = atClass.getDeclaredField("sCurrentActivityThread");
            atField.setAccessible(true);

            // 反射拿到ActivityThread对象
            Object activityThread = atField.get(null);

            // 反射拿到系统的handler属性
            Field mHField = atClass.getDeclaredField("mH");
            mHField.setAccessible(true);

            // 通过ActivityThread对象，反射拿到mH的值
            Handler mH = (Handler) mHField.get(activityThread);
            Field cbField = Handler.class.getDeclaredField("mCallback");
            cbField.setAccessible(true);
            // 将我们自定义的回调传给handler的callback，就不会调用自身的handleMessage方法
            cbField.set(mH, new HandlerCallback(mH));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "hookSysHandler: ", e);
        }
    }

    class HandlerCallback implements Handler.Callback {

        private Handler mH;

        public HandlerCallback(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                // 拦截startActivity方法
                Log.i(TAG, "handleMessage: startActivity 被拦截");
                handleLaunchActivity(msg);
            }
            mH.handleMessage(msg);
            return true;
        }

        private void handleLaunchActivity(Message msg) {
            try {
                Object obj = msg.obj;
                Field intentField = obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                // 反射拿到msg里的intent
                Intent proxyIntent = (Intent) intentField.get(obj);
                Intent realIntent = proxyIntent.getParcelableExtra("oldIntent");
                if (realIntent != null) {
                    // 把真正要启动的component设给intent
                    if (LoginManager.getInstance(mContext).isLogin()) {
                        proxyIntent.setComponent(realIntent.getComponent());
                    } else {
                        proxyIntent.setComponent(new ComponentName(mContext, LoginActivity.class));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
