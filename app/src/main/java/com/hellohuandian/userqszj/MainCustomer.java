package com.hellohuandian.userqszj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;

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
@EActivity(R.layout.main_customer)
public class MainCustomer extends BaseActivity {


    @ViewById
    LinearLayout page_return, to_c, phone_panel;
    @ViewById
    TextView phone_num;

    @AfterViews
    void afterViews() {
        lacksPermission();
        HttpGetPhone();
        progressDialog.show();
    }

    // 判断是否缺少权限
    private void lacksPermission() {
        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {

            int a = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE);

            if (a == -1) {
                String[] mPermissionList = new String[]{Manifest.permission.CALL_PHONE};
                ActivityCompat.requestPermissions(activity, mPermissionList, 1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void to_c() {
        to_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MainRecordEvaluate_.class);
                intent.putExtra("id", uid);
                activity.startActivity(intent);
            }
        });

    }

    @Click
    void phone_panel() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4006060137"));
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    /**
     * http接口：Service/getContact.html   获取电话信息
     */
    @Background
    void HttpGetPhone() {
        List<ParamTypeData> dataList = new ArrayList<>();
        new OkHttpConnect(activity, PubFunction.app + "Service/getContact.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetUserInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetUserInfo(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");

                    String contact_str = jsonObject.getString("contact");
                    final String contact = jsonObject.getString("_contact");

                    phone_num.setText(contact_str);
                    phone_panel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact));
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            startActivity(intent);
                        }
                    });
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

}

