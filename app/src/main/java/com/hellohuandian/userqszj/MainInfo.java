package com.hellohuandian.userqszj;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_info)
public class MainInfo extends BaseActivity {
    @ViewById
    LinearLayout page_return, authentication_panel, change_phoen;
    @ViewById
    TextView phoneView, authenticationView, content;
    @ViewById
    CircleImageView circleImageView;

    @AfterViews
    void afterViews() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString("id", "");
        if (PubFunction.isConnect(activity)) {
            HttpGetUserInfo_2();
            progressDialog.show();
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    /**
     * http接口：User/personal.html   获取用户信息
     */
    @Background
    void HttpGetUserInfo_2() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "User/personal.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetUserInfo_2(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetUserInfo_2(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    final JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                    final String img = jsonObject.getString("avater");
                    final String phone = jsonObject.getString("phone");
                    String str1 = phone.substring(0, 3);
                    String str2 = phone.substring(7, 11);
                    change_phoen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, MainInfoPhone_.class);
                            intent.putExtra("phone", phone);
                            activity.startActivity(intent);
                        }
                    });
                    phoneView.setText(str1 + " **** " + str2);
                    Picasso.with(activity).load(img).into(circleImageView);
                    content.setText("骑士换电祝您换电无忧，一路畅行");
                    authenticationView.setText(jsonObject.getString("id_auth_tip"));
                    if (authenticationView.getText().equals("未实名认证")) {
                        authentication_panel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                activity.startActivity(new Intent(activity, MainAuthentication_.class));
                            }
                        });
                    } else {
                        authentication_panel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String id_card = "";
                                String real_name = "";
                                String front_img = "";
                                String reverse_img = "";
                                try {
                                    real_name = jsonObject.getString("real_name");
                                    id_card = jsonObject.getString("id_card");
                                    reverse_img = jsonObject.getString("reverse_img");
                                    front_img = jsonObject.getString("front_img");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(activity, MainAuthenticationIS_.class);
                                intent.putExtra("id_card", id_card);
                                intent.putExtra("real_name", real_name);
                                intent.putExtra("front_img", front_img);
                                intent.putExtra("reverse_img", reverse_img);
                                activity.startActivity(intent);
                            }
                        });
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }
}