package com.hellohuandian.userqszj.pub;

import android.content.Context;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hellohuandian.userqszj.R;


/**
 * Created by hasee on 2017/6/8.
 */
public class MyToast {
    public static void showTheToast(Context context, String string) {
        try {
            Toast toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
            View view = LayoutInflater.from(context).inflate(R.layout.toast_panel, null);
            TextView textView = view.findViewById(R.id.text_1);
            textView.setText(string);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }

    }


}
