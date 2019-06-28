package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Registered")
@EActivity(R.layout.main_car)
public class MainCar extends BaseActivity {
    @ViewById
    LinearLayout page_return, panel_1, panel_2, panel_3, panel_4, panel_5, none_panel, data_panel;
    @ViewById
    TextView unbind, code, imei_code, time, is_online_state, t_4;
    @ViewById
    ImageView top_image;
    private String car_id = "";

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @AfterViews
    void afterViews() {
        reSetMain();
    }

    @UiThread
    void reSetMain() {
        if (PubFunction.isConnect(activity)) {
            HttpMyCarInfo();
            progressDialog.show();
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void panel_1() {
        if (PubFunction.isConnect(activity)) {
            Intent intent = new Intent(activity, MainCar_panel_1_.class);
            activity.startActivity(intent);
        }
    }

    @Click
    void panel_2() {
        if (PubFunction.isConnect(activity)) {
            Intent intent = new Intent(activity, MainCar_panel_2_.class);
            intent.putExtra("car_id", car_id);
            activity.startActivity(intent);
        }
    }

    @Click
    void panel_3() {
        if (PubFunction.isConnect(activity)) {
            Intent intent = new Intent(activity, MainCar_panel_3_.class);
            intent.putExtra("car_id", car_id);
            activity.startActivity(intent);
        }
    }

    @Click
    void panel_4() {
        HttpGetData(PubFunction.car_ID, "2");
    }

    @Click
    void panel_5() {
        HttpGetData(PubFunction.car_ID, "1");
    }

    @Click
    void unbind() {
        if (PubFunction.isConnect(activity)) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final AlertDialog mAlertDialog = builder.create();
            View view = inflater.inflate(R.layout.alertdialog_main_car, null);
            TextView title = view.findViewById(R.id.alertDialogTitle);
            title.setText("确定要解绑车辆？");
            TextView success = view.findViewById(R.id.payAlertdialogSuccess);
            success.setText("确定");
            success.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mAlertDialog.dismiss();
                    HttpUnBindCar(car_id);
                    progressDialog.show();
                }
            });
            TextView error = view.findViewById(R.id.payAlertdialogError);
            error.setText("取消");
            error.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog.show();
            mAlertDialog.getWindow().setContentView(view);
        }
    }

    /**
     * http接口：Device/myEcars     从服务器获得绑定车的数据
     */
    @Background
    void HttpMyCarInfo() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "Device/myEcars", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMyCarInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @SuppressLint("SetTextI18n")
    @UiThread
    void onDataHttpMyCarInfo(String response, String type) {
//        Util.d("0401301", "response: " + response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            none_panel.setVisibility(View.VISIBLE);
            data_panel.setVisibility(View.GONE);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    if (jsonObject_response.getString("data").equals("[]")) {
                        none_panel.setVisibility(View.VISIBLE);
                        data_panel.setVisibility(View.GONE);
                    } else {
                        JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                        none_panel.setVisibility(View.GONE);
                        data_panel.setVisibility(View.VISIBLE);
                        car_id = jsonObject.getString("sn");
                        imei_code.setText("设备ID：" + car_id);
                        String title = jsonObject.getString("title");
                        time.setText(title);
                        JSONArray jsonArray = jsonObject.getJSONArray("imgsUrl");
                        Picasso.with(activity).load(jsonArray.getString(0)).into(top_image);
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                    none_panel.setVisibility(View.VISIBLE);
                    data_panel.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
                none_panel.setVisibility(View.VISIBLE);
                data_panel.setVisibility(View.GONE);
            }
        }
    }

    /**
     * http接口: Device/unbindCar.html      解绑自己身上的车
     */
    @Background
    void HttpUnBindCar(String imei) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("sn", imei));
        new OkHttpConnect(activity, PubFunction.app + "Device/unbindCar.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpUnBindCar(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpUnBindCar(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                MyToast.showTheToast(activity, msg);
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口:汽车信息
     */
    @Background
    void HttpGetData(String pid, String type) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("url", "carAction!sendCommand.do"));
        dataList.add(new ParamTypeData("gps", car_id));
        dataList.add(new ParamTypeData("uid", uid));
        if (type.equals("1")) {
            dataList.add(new ParamTypeData("type", "1"));
            //断电
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("commandType", "2");
                jsonObject.put("commandName", "1");
                jsonObject.put("commandValue", "0");
                dataList.add(new ParamTypeData("param", jsonObject.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("关关");
        }
        if (type.equals("2")) {
            dataList.add(new ParamTypeData("type", "2"));
            //启动
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("commandType", "2");
                jsonObject.put("commandName", "1");
                jsonObject.put("commandValue", "1");
                dataList.add(new ParamTypeData("param", jsonObject.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("开开");
        }

        final String c_type = type;

        new OkHttpConnect(activity, PubFunction.app + "rpc/send.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetData(response, type, c_type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetData(String response, String type, String c_type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                System.out.println(jsonObject_response);
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                        if (jsonObject.has("res")) {
                            String msg = jsonObject.getString("res");
                            if (msg.equals("true")) {
                                if (c_type.equals("2")) {
                                    MyToast.showTheToast(activity, "下发启动指令成功");
                                } else if (c_type.equals("1")) {
                                    MyToast.showTheToast(activity, "下发刹车指令成功");
                                }
                            } else {
                                if (jsonObject.has("desc")) {
                                    MyToast.showTheToast(activity, jsonObject.getString("desc"));
                                } else {
                                    MyToast.showTheToast(activity, "服务器错误：HttpGetData");
                                }
                            }
                        } else {
                            MyToast.showTheToast(activity, "服务器错误：HttpGetData");
                        }
                    } else {
                        MyToast.showTheToast(activity, "登录太频繁！");
                    }
                } else {
                    String msg = jsonObject_response.getString("msg");
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }
}
