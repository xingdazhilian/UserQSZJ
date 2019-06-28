package com.hellohuandian.userqszj;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/6/5.
 */
@EActivity(R.layout.login)
public class Login extends BaseActivity {

    @ViewById
    TextView submit, get_number_text;
    @ViewById
    LinearLayout page_return, get_number;
    @ViewById
    EditText login_phone, login_number;
    @ViewById
    ImageView login_bg, top_image;

    Handler setTimeHandler;

    @AfterViews
    void afterViews() {
        init();
        handler();
    }

    private void handler() {
        setTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                int time_str = msg.getData().getInt("time");

                if (get_number_text != null) {
                    if (time_str == 0) {
                        get_number_text.setText("获取短信验证码");
                        get_number.setClickable(true);
                    } else {
                        get_number_text.setText(time_str + " S");
                        get_number.setClickable(false);
                    }
                }
            }
        };
    }

    private void init() {
        Picasso.with(activity).load(R.drawable.login_bg).memoryPolicy(MemoryPolicy.NO_CACHE).into(login_bg);
        Picasso.with(activity).load(R.drawable.wtf_image).memoryPolicy(MemoryPolicy.NO_CACHE).into(top_image);
    }

    @Click({R.id.submit, R.id.page_return, R.id.get_number})
    void click(View bt) {
        switch (bt.getId()) {
            case R.id.submit:
                String str_p = login_phone.getText().toString();
                String str_n = login_number.getText().toString();
                int str_p_l = str_p.length();
                if (str_p.equals("")) {
                    MyToast.showTheToast(this, "手机号不能为空！");
                } else if (str_n.equals("")) {
                    MyToast.showTheToast(this, "验证码不能为空！");
                } else if (str_p_l != 11) {
                    MyToast.showTheToast(this, "请输入正确的手机格式！");
                } else {
                    HttpLogin(str_p, str_n);
                    progressDialog.show();
                }
                break;
            case R.id.page_return:
                this.finish();
                break;
            case R.id.get_number:
                String str = login_phone.getText().toString();
                int str_l = str.length();
                if (str.equals("")) {
                    MyToast.showTheToast(this, "手机号不能为空！");
                } else if (str_l == 11) {
                    HttpGetCode(str);
                    TimerCount timerCount = new TimerCount(60, setTimeHandler);
                    progressDialog.show();
                    if (!timerCount.isAlive()) {
                        timerCount.start();
                    }
                } else {
                    MyToast.showTheToast(this, "请输入正确的手机格式！");
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    //获取验证码
    @Background
    void HttpGetCode(String mobile) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("phone", mobile));
        new OkHttpConnect(activity, PubFunction.api + "Sms/msgSend.html", dataList, HeaderTypeData.HEADER_Whit_APTK(activity), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetCode(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetCode(String response, String type) {
//        Util.d("033101", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                System.out.println(jsonObject);
                MyToast.showTheToast(activity, jsonObject.getString("msg"));
            } catch (Exception e) {
                e.printStackTrace();
                MyToast.showTheToast(activity, "JSON：" + e);
            }

        }
    }

    /**
     * http接口：Login/login.html    登陆
     */
    @UiThread
    void HttpLoginError(String messageString) {
        MyToast.showTheToast(activity, messageString);
    }

    @UiThread
    void HttpLoginSuccess(String str, String data) {
        String agreement_str = "";
        try {
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            String id = jsonObject.getString("id");
            String phone = jsonObject.getString("phone");
            String real_name = jsonObject.getString("real_name");
            String nick_name = jsonObject.getString("nick_name");
            String avater = jsonObject.getString("avater");
            String last_time = jsonObject.getString("last_time");
            String last_ip = jsonObject.getString("last_ip");
            String reg_time = jsonObject.getString("reg_time");
            String world = "";
            if (jsonObject.has("world")) {
                world = jsonObject.getString("world");
            }
            agreement_str = jsonObject.getString("agreement");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("id", id);
            editor.putString("phone", phone);
            editor.putString("real_name", real_name);
            editor.putString("nick_name", nick_name);
            editor.putString("avater", avater);
            editor.putString("last_time", last_time);
            editor.putString("last_ip", last_ip);
            editor.putString("reg_time", reg_time);
            editor.putString("agreement", agreement_str);
            editor.putString("world", world);
            editor.apply();
            //通知主页刷新数据
            Main.handleRefreshMarker.sendEmptyMessage(1);
            MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyToast.showTheToast(activity, str);
        if (agreement_str.equals("0")) {
            Intent intent = new Intent(activity, LoginAgreement_.class);
            activity.startActivity(intent);
        }
        activity.finish();
    }

    @UiThread
    void HttpReLoginSuccess(String str) {

        LayoutInflater inflater = LayoutInflater.from(this);
        final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
        View view = inflater.inflate(R.layout.alertdialog_bind, null);
        TextView title = view.findViewById(R.id.title);
        title.setText(str);
        TextView success_t = view.findViewById(R.id.success);
        success_t.setText("立即恢复");
        success_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_p = login_phone.getText().toString();
                String str_n = login_number.getText().toString();
                int str_p_l = str_p.length();
                if (str_p.equals("")) {
                    MyToast.showTheToast(activity, "手机号不能为空！");
                } else if (str_n.equals("")) {
                    MyToast.showTheToast(activity, "验证码不能为空！");
                } else if (str_p_l != 11) {
                    MyToast.showTheToast(activity, "请输入正确的手机格式！");
                } else {
                    HttpLogin_2(str_p, str_n);
                }
                dialog.dismiss();
            }
        });
        TextView error_t = view.findViewById(R.id.error);
        error_t.setText("注册新账号");
        error_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_p = login_phone.getText().toString();
                String str_n = login_number.getText().toString();
                int str_p_l = str_p.length();
                if (str_p.equals("")) {
                    MyToast.showTheToast(activity, "手机号不能为空！");
                } else if (str_n.equals("")) {
                    MyToast.showTheToast(activity, "验证码不能为空！");
                } else if (str_p_l != 11) {
                    MyToast.showTheToast(activity, "请输入正确的手机格式！");
                } else {
                    HttpLogin_null(str_p, str_n);
                }
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.show();
    }


    @Background
    void HttpLogin(String mobile, String code) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("phone", mobile));
        dataList.add(new ParamTypeData("vcode", code));
        dataList.add(new ParamTypeData("confirm", "1"));
        new OkHttpConnect(activity, PubFunction.app + "Login/login.html", dataList, HeaderTypeData.HEADER_Whit_APTK(activity), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLogin(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpLogin(String response, String type) {
//        Util.d("051701", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                System.out.println(jsonObject);
                String code_str = jsonObject.getString("status");
                String messageString = jsonObject.getString("msg");
                if (code_str.equals("1")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        HttpLoginSuccess(messageString, data);
                    } else {
                        HttpLoginSuccess(messageString, "");
                    }
                } else if (code_str.equals("2")) {
                    HttpReLoginSuccess(messageString);
                } else {
                    HttpLoginError(messageString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MyToast.showTheToast(activity, "JSON：" + e);
            }
        }
    }

    @Background
    void HttpLogin_2(String mobile, String code) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("phone", mobile));
        dataList.add(new ParamTypeData("vcode", code));
        dataList.add(new ParamTypeData("confirm", "2"));
        new OkHttpConnect(activity, PubFunction.app + "Login/login.html", dataList, HeaderTypeData.HEADER_Whit_APTK(activity), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLogin(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @Background
    void HttpLogin_null(String mobile, String code) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("phone", mobile));
        dataList.add(new ParamTypeData("vcode", code));
        dataList.add(new ParamTypeData("confirm", ""));
        new OkHttpConnect(activity, PubFunction.app + "Login/login.html", dataList, HeaderTypeData.HEADER_Whit_APTK(activity), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLogin(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    class TimerCount extends Thread {

        int count = 0;
        Handler handler;

        public TimerCount(int count, Handler handler) {
            this.count = count;
            this.handler = handler;
        }


        @Override
        public void run() {
            super.run();

            while (count > 0) {

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count = count - 1;
                if (handler != null) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("time", count);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

            }

        }
    }

}


