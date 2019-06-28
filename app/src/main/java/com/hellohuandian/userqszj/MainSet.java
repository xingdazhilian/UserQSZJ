package com.hellohuandian.userqszj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.fragment.LogoutDialogFragment;
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
@EActivity(R.layout.main_set)
public class MainSet extends BaseActivity {
    @ViewById
    LinearLayout page_return, about, agreement, cancellation, privacy_clause;
    @ViewById
    TextView logout, now_ver;
    private String message_code_str = "";

    @AfterViews
    void afterview() {
        if (PubFunction.isConnect(activity)) {
            HttpGetVer();
            progressDialog.show();
        }
    }

    @Click({R.id.page_return, R.id.logout, R.id.about, R.id.agreement, R.id.cancellation, R.id.privacy_clause})
    void click(View v) {
        if (v.getId() == R.id.page_return) {
            this.finish();
        } else if (v.getId() == R.id.logout) {
            LogoutDialogFragment logoutDialogFragment = new LogoutDialogFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            logoutDialogFragment.setOnDialogItemClickListener(new LogoutDialogFragment.OnDialogItemClickListener() {
                @Override
                public void onSuccessClick() {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    activity.finish();
                }

                @Override
                public void onErrorClick() {
                }
            });
            logoutDialogFragment.show(fragmentTransaction, "LOGOUT");
        } else if (v.getId() == R.id.about) {
            startActivity(new Intent(activity, MainAbout_.class));
        } else if (v.getId() == R.id.agreement) {
            Intent intent = new Intent(activity, MainAgreement_.class);
            activity.startActivity(intent);
        } else if (v.getId() == R.id.cancellation) {


            LayoutInflater inflater = LayoutInflater.from(this);
            final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
            View view = inflater.inflate(R.layout.alertdialog_cancellation, null);


            final EditText input_code = (EditText) view.findViewById(R.id.input_code);
            final TextView get_code = (TextView) view.findViewById(R.id.get_code);

            get_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phone = sharedPreferences.getString("phone", "");
                    if (phone.equals("")) {
                        MyToast.showTheToast(activity, "手机参数错误！");
                    } else {
                        if (PubFunction.isConnect(activity)) {
                            HttpGetCode(phone);
                        }
                    }
                }
            });

            TextView success_t = (TextView) view.findViewById(R.id.success);
            success_t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String code_str = input_code.getText().toString().trim();
                    message_code_str = code_str;
                    HttpUserCancel(code_str, "0");
                    dialog.dismiss();
                    PubFunction.hide_keyboard_from(activity, input_code);
                }
            });
            TextView error_t = (TextView) view.findViewById(R.id.error);
            error_t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    PubFunction.hide_keyboard_from(activity, input_code);
                }
            });
            dialog.setCancelable(false);
            dialog.setContentView(view);
            dialog.show();

        } else if (v.getId() == R.id.privacy_clause) {
            startActivity(new Intent(activity, MainPrivacyClause_.class));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    /**
     * http接口：App/version.html   获取当前版本号
     */

    @Background
    void HttpGetVer() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.api + "AppVer/version.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetVer(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetVer(String response, String type) {
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
                    now_ver.setText("V" + jsonObject.getString("Android_name"));

                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：Sms/msgSend.html    获取短信验证码
     */
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
     * http接口：User/cancel.html    注销用户
     */
    @Background
    void HttpUserCancel(String code_str, String confirm) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("code", code_str));
        if (confirm.equals("0")) {
        } else if (confirm.equals("1")) {
            dataList.add(new ParamTypeData("confirm", "1"));
        }
        new OkHttpConnect(activity, PubFunction.app + "User/cancel.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpUserCancel(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpUserCancel(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String messageString = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("2")) {
                    if (messageString.indexOf("如果确定注销将丢失") != -1 || messageString.indexOf("您确定注销") != -1) {
                        LayoutInflater inflater = LayoutInflater.from(activity);
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        final AlertDialog mAlertDialog = builder.create();
                        View view = inflater.inflate(R.layout.alertdialog_cancellation_2, null);

                        TextView alertDialogTitle = (TextView) view.findViewById(R.id.alertDialogTitle);
                        alertDialogTitle.setText(messageString);

                        TextView success_t = (TextView) view.findViewById(R.id.success);
                        success_t.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PubFunction.isConnect(activity)) {
                                    HttpUserCancel(message_code_str, "1");
                                    mAlertDialog.dismiss();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.commit();
                                    activity.finish();

                                }
                            }
                        });
                        TextView error_t = (TextView) view.findViewById(R.id.error);
                        error_t.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        });

                        mAlertDialog.setCancelable(false);
                        mAlertDialog.show();
                        mAlertDialog.getWindow().setContentView(view);
                    } else {
                        MyToast.showTheToast(activity, messageString);
                    }
                } else {
                    MyToast.showTheToast(activity, messageString);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }
}
