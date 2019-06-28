package com.hellohuandian.userqszj;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.hellohuandian.userqszj.extend_plug.StatusBar.StatusBar;
import com.hellohuandian.userqszj.pub.ProgressDialog;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class BaseFragmentActivity extends FragmentActivity {

    protected Activity activity;
    protected ProgressDialog progressDialog;
    protected SharedPreferences sharedPreferences;
    protected String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        sharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString("id", "");
        progressDialog = new ProgressDialog(this);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        new StatusBar(this, 1); // 沉浸式状态栏 初始化 每个activity和fragement都应该有
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(0x33000000);

    }
}
