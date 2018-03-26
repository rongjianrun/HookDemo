package com.rjr.hookdemo;

import android.app.Application;

import com.rjr.hookdemo.utils.HookUtil;

/**
 * Created by Administrator on 2018/3/26.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil.getInstance(this).hookAms();
        HookUtil.getInstance(this).hookSysHandler();
    }
}
