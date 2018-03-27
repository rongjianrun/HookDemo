package com.rjr.hookdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rjr.hookdemo.utils.LoginManager;

/**
 * Created by Administrator on 2018/3/26.
 */

public class LoginActivity extends BaseActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Intent realIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);

        realIntent = getIntent().getParcelableExtra("oldIntent");
    }

    public void login(View view) {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "用户名跟密码不能为空", Toast.LENGTH_SHORT).show();
        } else {
            LoginManager.getInstance(this).login(username, password);
            if (realIntent != null) {
                startActivity(realIntent);
                finish();
            }
        }
    }
}
