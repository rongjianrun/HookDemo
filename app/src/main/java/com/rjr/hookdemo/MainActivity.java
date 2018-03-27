package com.rjr.hookdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.rjr.hookdemo.utils.LoginManager;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toOrder(View view) {
        startActivity(new Intent(this, OrderActivity.class));
    }

    public void toShopping(View view) {
        startActivity(new Intent(this, ShoppingActivity.class));
    }

    public void logout(View view) {
        LoginManager.getInstance(this).logout();
    }
}
