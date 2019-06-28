package com.hellohuandian.userqszj;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;

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


@EActivity(R.layout.main_message_4)
public class MainMessage_4 extends BaseActivity {

    @ViewById
    LinearLayout page_return, panel_1, panel_2, panel_3, none_panel;
    @ViewById
    TextView panel_1_name, panel_2_name, panel_3_name;
    @ViewById
    ImageView panel_1_point, panel_2_point, panel_3_point;
    @ViewById
    PullToRefreshLayout refresh;

    @AfterViews
    void afterviews() {
        refresh.setCanLoadMore(false);
        refresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                HttpGetMessageCount();
                progressDialog.show();
            }

            @Override
            public void loadMore() {

            }
        });
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpGetMessageCount();
        progressDialog.show();
    }

    /**
     * http接口：Advices/type.html    获取消息信息
     */
    @Background
    void HttpGetMessageCount() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "Advices/type.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetMessageCount(response, type);

                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetMessageCount(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    none_panel.setVisibility(View.GONE);

                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");

                    JSONObject system_jsonObject = jsonObject.getJSONObject("system");
                    String system_jsonObject_name = system_jsonObject.getString("name");
                    String system_jsonObject_type = system_jsonObject.getString("type");
                    String system_jsonObject_num = system_jsonObject.getString("num");
                    panel_1_name.setText(system_jsonObject_name);
                    panel_1_name.setTag(system_jsonObject_type.toString());
                    int system_jsonObject_num_int = Integer.parseInt(system_jsonObject_num);
                    if (system_jsonObject_num_int != 0) {
                        panel_1_point.setVisibility(View.VISIBLE);
                    } else {
                        panel_1_point.setVisibility(View.GONE);
                    }
                    panel_1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(activity, MainMessage_4_Info_1_.class);
                            intent.putExtra("type", panel_1_name.getTag().toString());
                            intent.putExtra("type_name", panel_1_name.getText().toString());
                            activity.startActivity(intent);

                        }
                    });
                    JSONObject warning_jsonObject = jsonObject.getJSONObject("warning");
                    String warning_jsonObject_name = warning_jsonObject.getString("name");
                    String warning_jsonObject_type = warning_jsonObject.getString("type");
                    String warning_jsonObject_num = warning_jsonObject.getString("num");
                    panel_2_name.setText(warning_jsonObject_name);
                    panel_2_name.setTag(warning_jsonObject_type.toString());
                    int warning_jsonObject_num_int = Integer.parseInt(warning_jsonObject_num);
                    if (warning_jsonObject_num_int != 0) {
                        panel_2_point.setVisibility(View.VISIBLE);
                    } else {
                        panel_2_point.setVisibility(View.GONE);
                    }
                    panel_2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(activity, MainMessage_4_Info_2_.class);
                            intent.putExtra("type", panel_2_name.getTag().toString());
                            intent.putExtra("type_name", panel_2_name.getText().toString());
                            activity.startActivity(intent);

                        }
                    });
                    JSONObject activity_jsonObject = jsonObject.getJSONObject("activity");
                    String activity_jsonObject_name = activity_jsonObject.getString("name");
                    String activity_jsonObject_type = activity_jsonObject.getString("type");
                    String activity_jsonObject_num = activity_jsonObject.getString("num");
                    panel_3_name.setText(activity_jsonObject_name);
                    panel_3_name.setTag(activity_jsonObject_type.toString());
                    int activity_jsonObject_num_int = Integer.parseInt(activity_jsonObject_num);
                    if (activity_jsonObject_num_int != 0) {
                        panel_3_point.setVisibility(View.VISIBLE);
                    } else {
                        panel_3_point.setVisibility(View.GONE);
                    }
                    panel_3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(activity, MainMessage_4_Info_3_.class);
                            intent.putExtra("type", panel_3_name.getTag().toString());
                            intent.putExtra("type_name", panel_3_name.getText().toString());
                            activity.startActivity(intent);

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