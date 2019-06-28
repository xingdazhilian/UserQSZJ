package com.hellohuandian.userqszj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.hellohuandian.userqszj.BaseActivity;
import com.hellohuandian.userqszj.Main_;

public class Index extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //延迟2s跳转
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Index.this, Main_.class);
                startActivity(intent);
                finish();
            }
        }, 900);
    }

}
