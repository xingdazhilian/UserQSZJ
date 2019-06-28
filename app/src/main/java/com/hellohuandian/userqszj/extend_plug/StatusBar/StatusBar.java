package com.hellohuandian.userqszj.extend_plug.StatusBar;

import android.app.Activity;
import android.view.WindowManager;

public class StatusBar {

    private Activity activity;

    public StatusBar(Activity activity) {
        this.activity = activity;

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }


    public StatusBar(Activity activity, int type) {
        this.activity = activity;


        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (type == 0) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


}
