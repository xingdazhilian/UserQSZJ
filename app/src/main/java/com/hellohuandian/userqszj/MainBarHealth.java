package com.hellohuandian.userqszj;

import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_bar_health)
public class MainBarHealth extends BaseActivity {

    @ViewById
    LinearLayout page_return, bar_list;
    @ViewById
    CircularProgressBar prog;
    @ViewById
    TextView t_1, t_2;

    @AfterViews
    void afterViews() {
        HttpLifeTime();
        progressDialog.show();
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void bar_list() {
        activity.startActivity(new Intent(activity, MainBarHealthList_.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Background
    void HttpLifeTime() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "User/lifeTime.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLifeTime(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpLifeTime(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");

                    String total = jsonObject.getString("total");
                    String surplus = jsonObject.getString("surplus");
                    String rate = jsonObject.getString("rate");

                    int rate_int = Integer.parseInt(rate);
                    prog.setProgress(rate_int);
                    t_1.setText(surplus);
                    t_2.setText("总循环 " + total + " 次");
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }
}
