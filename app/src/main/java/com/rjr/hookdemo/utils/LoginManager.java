package com.rjr.hookdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/3/27.
 */

public class LoginManager {

    private static LoginManager loginManager;
    private final SharedPreferences mainCache;
    private final SharedPreferences.Editor edit;
    private final Context context;

    private LoginManager(Context context) {
        this.context = context;
        mainCache = context.getSharedPreferences("main_cache", Context.MODE_PRIVATE);
        edit = mainCache.edit();
    }

    public static LoginManager getInstance(Context context) {
        if (loginManager == null) {
            synchronized (LoginManager.class) {
                if (loginManager == null) {
                    loginManager = new LoginManager(context.getApplicationContext());
                }
            }
        }
        return loginManager;
    }

    public void login(String username, String password) {
        if (TextUtils.equals("admin", username) && TextUtils.equals("123", password)) {
            edit.putString("username", username)
                    .putString("password", password)
                    .commit();
        } else {
            Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    public void logout() {
        edit.remove("username")
                .remove("password")
                .commit();
    }

    public boolean isLogin() {
        String username = mainCache.getString("username", null);
        String password = mainCache.getString("password", null);
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }
}
