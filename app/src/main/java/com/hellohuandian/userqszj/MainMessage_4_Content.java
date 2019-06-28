package com.hellohuandian.userqszj;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.squareup.picasso.Picasso;

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


@EActivity(R.layout.main_message_4_al)
public class MainMessage_4_Content extends BaseActivity {

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView text_1, text_2;
    @ViewById
    ImageView i_1;

    private String id = "";
    private String image_url = "";

    @AfterViews
    void afterViews() {

        text_1.setText(getIntent().getStringExtra("t_1"));
        text_2.setText(getIntent().getStringExtra("t_2"));

        id = getIntent().getStringExtra("id");
        image_url = getIntent().getStringExtra("image");
        if (!image_url.equals("")) {
            Picasso.with(activity).load(image_url).into(i_1);
        } else {
            i_1.setVisibility(View.GONE);
        }
        if (PubFunction.isConnect(activity)) {
            HttpIsRead(id);
            progressDialog.show();
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    /**
     * http接口：Advices/editStatus    获取消息信息
     */
    @Background
    void HttpIsRead(String id_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("id", id_str));
        new OkHttpConnect(activity, PubFunction.app + "Advices/editStatus", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpIsRead(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpIsRead(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");

                if (status.equals("1")) {

                } else {
                    MyToast.showTheToast(activity, msg);
                }

            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }
}