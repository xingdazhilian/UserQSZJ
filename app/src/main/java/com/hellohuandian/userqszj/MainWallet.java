package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.scanCode.ScanCodeActivity_;
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
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_wallet)
public class MainWallet extends BaseActivity {

    @ViewById
    LinearLayout page_return, bar_health, send_coin_panel, coin_panel, select_order_panel, none_panel;
    @ViewById
    ListView listview;
    @ViewById
    TextView coin, coin_1, submit, select_order;

    MyBaseAdapter myBaseAdapter;
    List<Map<String, String>> datalist_1 = new ArrayList<>();

    private String car_did = "";
    private String bar_id = "";
    private String cha_id = "";

    private Handler changeDataHandler;

    @AfterViews
    void afterViews() {
        handler();
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        changeDataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String position_str = msg.getData().getString("position");
                int position = Integer.parseInt(position_str);

                String is_select_v = datalist_1.get(position).get("is_select");
                System.out.println(datalist_1.get(position).get("name"));
                if (is_select_v.equals("0")) {
                    datalist_1.get(position).put("is_select", "1");
                } else if (is_select_v.equals("1")) {
                    datalist_1.get(position).put("is_select", "0");
                }

                int firstVisiblePosition = listview.getFirstVisiblePosition(); /**最后一个可见的位置**/
                int lastVisiblePosition = listview.getLastVisiblePosition(); /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
                if (position >= firstVisiblePosition && position <= lastVisiblePosition) { /**获取指定位置view对象**/
                    View view = listview.getChildAt(position - firstVisiblePosition);
                    ImageView i_2 = (ImageView) view.findViewById(R.id.i_2);
                    String is_select = datalist_1.get(position).get("is_select");
                    if (is_select.equals("0")) {
                        i_2.setImageResource(R.drawable.qianbao2);
                    } else if (is_select.equals("1")) {
                        i_2.setImageResource(R.drawable.qianbao3);
                    }

                }

                int j = 0;
                for (int i = 0; i < datalist_1.size(); i++) {
                    String is_selected = datalist_1.get(i).get("is_select");
                    if (is_selected != null) {
                        if (is_selected.equals("1") || is_selected.equals("2")) {
                            j = 1;
                            break;
                        }
                    }
                }
                if (j == 0) {
                    select_order_panel.setVisibility(View.GONE);
                } else if (j == 1) {
                    select_order_panel.setVisibility(View.VISIBLE);
                }


            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PubFunction.isConnect(activity)) {
            HttpGetWalletInfo();
        }
        select_order_panel.setVisibility(View.GONE);
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void bar_health() {
        activity.startActivity(new Intent(activity, MainBarHealth_.class));
    }

    @Click
    void select_order() {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < datalist_1.size(); i++) {
                Map<String, String> dataMap = datalist_1.get(i);
                if (dataMap.get("is_select") != null) {
                    if (dataMap.get("is_select").equals("1") || dataMap.get("is_select").equals("2")) {
                        String devid = dataMap.get("id");
                        String otype = dataMap.get("type");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("devid", devid);
                        jsonObject.put("otype", otype);
                        jsonArray.put(jsonObject);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(activity, MainShopOrderV2_.class);
        intent.putExtra("mjson", jsonArray.toString());
        intent.putExtra("type", "20");
        activity.startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0005 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                if (PubFunction.isConnect(activity)) {

                    if (!car_did.equals("")) {
                        HttpBindCar(car_did, content);
                        car_did = "";
                        progressDialog.dismiss();
                    }
                }

            }
        }

        if (requestCode == 0x0006 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                if (PubFunction.isConnect(activity)) {

                    if (!cha_id.equals("")) {
                        HttpBindCha(cha_id, content);
                        cha_id = "";
                        progressDialog.dismiss();
                    }
                }

            }
        }

        if (requestCode == 0x00010 && resultCode == RESULT_OK) {
            if (data != null) {
                String sourceStr = data.getStringExtra("codedContent");
                if (PubFunction.isConnect(activity)) {
                    if (!bar_id.equals("")) {

                        int count = 0;
                        for (int i = 0; i < sourceStr.length(); i++) {
                            String s = String.valueOf(sourceStr.charAt(i)); //char 类型转String
                            if (s.equals("/")) {
                                count++;
                            }
                        }
                        if (count == 2) {
                            String[] sourceStrArray = sourceStr.split("/");

                            if (PubFunction.isConnect(activity, false)) {

                                HttpBindBar(bar_id, sourceStrArray[0]);
                                bar_id = "";
                                progressDialog.dismiss();

                            }
                        } else {
                            MyToast.showTheToast(activity, "请扫描柜子上面的二维码！");
                        }
                    }
                }
            }
        }
    }


    @Click
    void submit() {
        activity.startActivity(new Intent(activity, MainShop_.class));
    }


    /**
     * http接口：User/walletv2.html  获取钱包信息
     */
    @Background
    void HttpGetWalletInfo() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
//        Util.d("032901", "uid: " + uid);
        new OkHttpConnect(activity, PubFunction.app + "User/walletv3.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetWalletInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetWalletInfo(String response, String type) {
//        Util.d("0329021", response);
        datalist_1.clear();
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
                    String hello_str = jsonObject.getString("hello");
                    coin.setText(hello_str);
                    String hello_free_str = jsonObject.getString("hello_free");
                    coin_1.setText(hello_free_str);
                    if (hello_free_str.equals("0")) {
                        send_coin_panel.setVisibility(View.GONE);
                        coin_panel.setGravity(Gravity.CENTER);
                    }
                    String bike_str = jsonObject.getString("bike");
                    if (bike_str.equals("") || bike_str.equals("[]") || bike_str.equals("[[]]")) {
                    } else {
                        JSONArray jsonArray_bike = jsonObject.getJSONArray("bike");
                        for (int i = 0; i < jsonArray_bike.length(); i++) {
                            JSONObject jsonObject_item = jsonArray_bike.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject_item.getString("devid"));
                            map.put("name", jsonObject_item.getString("name"));
                            map.put("type", jsonObject_item.getString("otype"));
                            map.put("number", jsonObject_item.getString("number"));
                            map.put("rprice", jsonObject_item.getString("rprice"));
                            map.put("use_type", jsonObject_item.getString("use_type"));
                            map.put("bg_img", jsonObject_item.getString("bg_img"));
                            map.put("is_bind", jsonObject_item.getString("is_bind"));
                            map.put("btips", jsonObject_item.getString("btips"));
                            map.put("remark", jsonObject_item.getString("remark"));
                            map.put("fade_status", jsonObject_item.getString("fade_status"));
                            map.put("shop_type", jsonObject_item.getString("shop_type"));
                            map.put("end_ts", jsonObject_item.getString("end_ts"));
                            map.put("checked", jsonObject_item.getString("checked"));
                            map.put("mrent", jsonObject_item.getString("mrent"));
                            map.put("view_type", "1");

                            if (jsonObject_item.getString("checked").equals("0")) {
                                map.put("is_select", "0");
                            } else if (jsonObject_item.getString("checked").equals("1")) {
                                map.put("is_select", "1");
                            } else if (jsonObject_item.getString("checked").equals("2")) {
                                map.put("is_select", "2");
                            }
                            datalist_1.add(map);
                        }
                    }

                    String charger_str = jsonObject.getString("charger");
                    if (charger_str == null || charger_str.equals("") || charger_str.equals("[]") || charger_str.equals("[[]]")) {
                    } else {
                        JSONArray jsonArray_charger = jsonObject.getJSONArray("charger");
                        for (int i = 0; i < jsonArray_charger.length(); i++) {
                            JSONObject jsonObject_item = jsonArray_charger.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject_item.getString("devid"));
                            map.put("name", jsonObject_item.getString("name"));
                            map.put("use_type", jsonObject_item.getString("use_type"));
                            map.put("number", jsonObject_item.getString("number"));
                            map.put("rprice", jsonObject_item.getString("rprice"));
                            map.put("bg_img", jsonObject_item.getString("bg_img"));
                            map.put("is_bind", jsonObject_item.getString("is_bind"));
                            map.put("btips", jsonObject_item.getString("btips"));
                            map.put("fade_status", jsonObject_item.getString("fade_status"));
                            map.put("shop_type", jsonObject_item.getString("shop_type"));
                            if (jsonObject_item.has("otype")) {
                                map.put("type", jsonObject_item.getString("otype"));
                            } else {
                                map.put("type", "121");
                            }
                            if (jsonObject_item.has("utips")) {
                                map.put("utips", jsonObject_item.getString("utips"));
                            } else {
                                map.put("utips", "");
                            }
                            if (jsonObject_item.has("sign")) {
                                map.put("sign", jsonObject_item.getString("sign"));
                            } else {
                                map.put("sign", "");
                            }
                            if (jsonObject_item.has("remark")) {
                                map.put("remark", jsonObject_item.getString("remark"));
                            } else {
                                map.put("remark", "");
                            }
                            map.put("is_select", "0");
                            map.put("end_ts", jsonObject_item.getString("end_ts"));
                            map.put("view_type", "1");
                            datalist_1.add(map);
                        }
                    }

                    String device_str = jsonObject.getString("battery");
                    if (device_str.equals("") || device_str.equals("[]") || device_str.equals("[[]]")) {
                    } else {
                        JSONArray jsonArray_device = jsonObject.getJSONArray("battery");
                        for (int i = 0; i < jsonArray_device.length(); i++) {
                            JSONObject jsonObject_item = jsonArray_device.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject_item.getString("devid"));
                            map.put("name", jsonObject_item.getString("name"));
                            map.put("type", jsonObject_item.getString("otype"));
                            map.put("number", jsonObject_item.getString("number"));
                            map.put("rprice", jsonObject_item.getString("rprice"));
                            map.put("use_type", jsonObject_item.getString("use_type"));
                            map.put("bg_img", jsonObject_item.getString("bg_img"));
                            map.put("is_bind", jsonObject_item.getString("is_bind"));
                            map.put("btips", jsonObject_item.getString("btips"));
                            map.put("remark", jsonObject_item.getString("remark"));
                            map.put("fade_status", jsonObject_item.getString("fade_status"));
                            map.put("shop_type", jsonObject_item.getString("shop_type"));
                            map.put("end_ts", jsonObject_item.getString("end_ts"));
                            map.put("checked", jsonObject_item.getString("checked"));
                            map.put("mrent", jsonObject_item.getString("mrent"));

                            if (jsonObject_item.getString("checked").equals("0")) {
                                map.put("is_select", "0");
                            } else if (jsonObject_item.getString("checked").equals("1")) {
                                map.put("is_select", "1");
                            } else if (jsonObject_item.getString("checked").equals("2")) {
                                map.put("is_select", "2");
                            }
                            map.put("view_type", "1");
                            datalist_1.add(map);
                        }
                    }

                    String packs_str = jsonObject.getString("packets");
                    if (packs_str.equals("") || packs_str.equals("[]") || packs_str.equals("[[]]")) {
                        Map<String, String> map = new HashMap<>();
                        map.put("type", "30");
                        map.put("view_type", "2");
                        datalist_1.add(map);
                    } else {
                        JSONObject jsonObject_pack = jsonObject.getJSONObject("packets");
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject_pack.getString("gid"));
                        map.put("type", jsonObject_pack.getString("otype"));
                        map.put("name", jsonObject_pack.getString("name"));
                        map.put("rprice", jsonObject_pack.getString("rprice"));
                        map.put("packet", jsonObject_pack.getString("packet"));
                        map.put("expire", jsonObject_pack.getString("expire"));
                        map.put("bg_img", jsonObject_pack.getString("bg_img"));
                        map.put("tips", jsonObject_pack.getString("tips"));
                        if (jsonObject_pack.has("mains")) {
                            map.put("mains", jsonObject_pack.getString("mains"));
                        }
                        map.put("is_select", "0");
                        map.put("view_type", "2");
                        datalist_1.add(map);
                    }

                    String cards_str = jsonObject.getString("cards");
                    if (cards_str.equals("") || cards_str.equals("[]") || cards_str.equals("[[]]")) {
                    } else {
                        JSONArray jsonArray_cards = jsonObject.getJSONArray("cards");
                        for (int i = 0; i < jsonArray_cards.length(); i++) {
                            JSONObject jsonObject_item = jsonArray_cards.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject_item.getString("gid"));
                            map.put("type", jsonObject_item.getString("otype"));
                            map.put("name", jsonObject_item.getString("name"));
                            map.put("rprice", jsonObject_item.getString("rprice"));
                            map.put("start_ts", jsonObject_item.getString("start_ts"));
                            map.put("end_ts", jsonObject_item.getString("end_ts"));
                            map.put("content", jsonObject_item.getString("content"));
                            map.put("remark", jsonObject_item.getString("remark"));
                            map.put("rule", jsonObject_item.getString("rule"));
                            map.put("expire", jsonObject_item.getString("expire"));
                            map.put("days", jsonObject_item.getString("days"));
                            map.put("bg_img", jsonObject_item.getString("bg_img"));
                            map.put("is_select", "0");
                            //这里的view_type是我自己加的，为2的时候指：包月换电、年卡
                            map.put("view_type", "2");
                            datalist_1.add(map);
                        }
                    }

                    myBaseAdapter = new MyBaseAdapter(activity, datalist_1, new int[]{R.layout.main_wallet_item_1, R.layout.main_wallet_item_2, R.layout.main_wallet_item_3, R.layout.main_wallet_item_4});
                    listview.setAdapter(myBaseAdapter);
                    listview.setDividerHeight(0);

                    int j = 0;
                    if (datalist_1.size() > 1) {
                        for (int i = 0; i < datalist_1.size(); i++) {
                            String is_selected = datalist_1.get(i).get("is_select");
                            if (is_selected != null) {
                                if ((is_selected.equals("1") || is_selected.equals("2"))) {
                                    j = 1;
                                    break;
                                }
                            }
                        }
                        if (j == 0) {
                            select_order_panel.setVisibility(View.GONE);
                        } else {
                            select_order_panel.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：Device/bindBar.html     绑定电池到自己身上
     */
    @Background
    void reLoading() {
        try {
            sleep(5000);
            HttpGetWalletInfo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Background
    void HttpBindBar(String did, String sn) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("did", did));
        dataList.add(new ParamTypeData("number", sn));

        new OkHttpConnect(activity, PubFunction.api + "Rent/bindv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpBindBar(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpBindBar(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            HttpGetWalletInfo();
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    reLoading();
                    MyToast.showTheToast(activity, msg);
                } else {
                    MyToast.showTheToast(activity, msg);
                    HttpGetWalletInfo();
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
                HttpGetWalletInfo();
            }
        }
    }

    /**
     * http接口：Device/bindCar.html     绑定车到自己身上
     */
    @Background
    void HttpBindCar(String did, String sn) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("did", did));
        dataList.add(new ParamTypeData("sn", sn));

        new OkHttpConnect(activity, PubFunction.app + "Device/bindCar.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpBindCar(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpBindCar(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            HttpGetWalletInfo();
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    reLoading();
                    MyToast.showTheToast(activity, msg);
                } else {
                    MyToast.showTheToast(activity, msg);
                    HttpGetWalletInfo();
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
                HttpGetWalletInfo();
            }
        }
    }

    /**
     * http接口：Device/bindCha.html     绑定车到自己身上
     */
    @Background
    void HttpBindCha(String did, String sn) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("did", did));
        dataList.add(new ParamTypeData("number", sn));
        new OkHttpConnect(activity, PubFunction.app + "Charger/bindv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpBindCha(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpBindCha(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            HttpGetWalletInfo();
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    reLoading();
                    MyToast.showTheToast(activity, msg);
                } else {
                    MyToast.showTheToast(activity, msg);
                    HttpGetWalletInfo();
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
                HttpGetWalletInfo();
            }
        }
    }

    private void HttpIsSure_1(final Map<String, String> data) {
        List<ParamTypeData> list = new ArrayList<>();
        list.add(new ParamTypeData("uid", uid + ""));
        list.add(new ParamTypeData("id", data.get("id")));
        list.add(new ParamTypeData("type", data.get("type")));
        list.add(new ParamTypeData("confirm", "1"));
        progressDialog.show();
        new OkHttpConnect(activity, PubFunction.app + "User/confirm.html", list, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpIsSure_1(response, type, data);
                progressDialog.dismiss();

            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpIsSure_1(String response, String type, final Map<String, String> data) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                System.out.println(jsonObject.toString());
                String status = jsonObject.getString("status");
                String msg = jsonObject.getString("msg");
                if (status.equals("2")) {

                    LayoutInflater inflater = LayoutInflater.from(this);
                    final Dialog dialog = new Dialog(this, R.style.Translucent_NoTitle);
                    View view = inflater.inflate(R.layout.alertdialog_bind, null);

                    TextView title = (TextView) view.findViewById(R.id.title);
                    title.setText(msg);

                    TextView success_t = (TextView) view.findViewById(R.id.success);
                    success_t.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HttpIsSure_2(data);
                            dialog.dismiss();
                        }
                    });
                    TextView error_t = (TextView) view.findViewById(R.id.error);
                    error_t.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setContentView(view);
                    dialog.show();

                } else if (status.equals("1")) {
                    MyToast.showTheToast(activity, msg);
                    if (PubFunction.isConnect(activity)) {
                        HttpGetWalletInfo();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                MyToast.showTheToast(activity, "JSON：" + e);
            }
        }
    }

    private void HttpIsSure_2(Map<String, String> data) {
        List<ParamTypeData> list = new ArrayList<>();
        list.add(new ParamTypeData("uid", uid + ""));
        list.add(new ParamTypeData("id", data.get("id")));
        list.add(new ParamTypeData("type", data.get("type")));
        list.add(new ParamTypeData("confirm", "0"));
        progressDialog.show();
        new OkHttpConnect(activity, PubFunction.app + "User/confirm.html", list, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpIsSure_2(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpIsSure_2(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                System.out.println(jsonObject.toString());
                String status = jsonObject.getString("status");
                String msg = jsonObject.getString("msg");
                MyToast.showTheToast(activity, msg);
                if (PubFunction.isConnect(activity)) {
                    HttpGetWalletInfo();
                }
            } catch (Exception e) {
                e.printStackTrace();
                MyToast.showTheToast(activity, "JSON：" + e);
            }
        }
    }

    class MyBaseAdapter extends BaseAdapter {
        private Context context;
        private List<? extends Map<String, ?>> data;
        private int[] resource;

        public MyBaseAdapter(Context context, List<? extends Map<String, ?>> data, int[] resource) {
            this.context = context;
            this.data = data;
            this.resource = resource;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            final int count = position;
            String view_type = data.get(position).get("view_type").toString();

            if (view_type.equals("1")) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                view = inflater.inflate(resource[0], null);

                TextView t_1 = view.findViewById(R.id.t_1);
                TextView t_2 = view.findViewById(R.id.t_2);
                TextView t_3 = view.findViewById(R.id.t_3);
                TextView t_4 = view.findViewById(R.id.t_4);
                TextView t_5 = view.findViewById(R.id.t_5);
                TextView t_6 = view.findViewById(R.id.t_6);
                TextView t_7 = view.findViewById(R.id.t_7);

                LinearLayout l_1 = view.findViewById(R.id.l_1);

                ImageView i_1 = view.findViewById(R.id.i_1);
                ImageView i_2 = view.findViewById(R.id.i_2);
                ImageView i_3 = view.findViewById(R.id.i_3);
                Picasso.with(activity).load(data.get(position).get("bg_img").toString()).into(i_3);

                String use_type = data.get(position).get("use_type").toString();
                if (use_type.equals("10")) { //买
                    t_1.setText(data.get(position).get("name").toString());
                    t_2.setText(data.get(position).get("btips").toString());
                    t_3.setText("编号：" + data.get(position).get("number").toString());
                    t_4.setText("购买时间：" + data.get(position).get("end_ts").toString());

                    t_6.setVisibility(View.GONE);
                    t_7.setVisibility(View.GONE);
                    l_1.setVisibility(View.GONE);
                    i_2.setVisibility(View.GONE);
                    i_2.setTag(position + "");
                } else if (use_type.equals("20")) { //租
                    t_1.setText(data.get(position).get("name").toString());
                    t_2.setText(data.get(position).get("btips").toString());
                    i_2.setTag(position + "");
                    t_3.setText("编号：" + data.get(position).get("number").toString());
                    t_4.setText("押金：" + data.get(position).get("rprice").toString() + "元");
                    t_6.setText("月租：" + data.get(position).get("mrent").toString() + "元");
                    t_7.setText("到期时间：" + data.get(position).get("end_ts").toString());

                    final String is_select = data.get(position).get("is_select").toString();
                    if (is_select.equals("0")) {
                        i_2.setImageResource(R.drawable.qianbao2);
                    } else if (is_select.equals("1")) {
                        i_2.setImageResource(R.drawable.qianbao3);
                    } else if (is_select.equals("2")) {
                        i_2.setImageResource(R.drawable.qianbao3);
                    }

                    i_2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("position", view.getTag().toString());
                            message.setData(bundle);
                            changeDataHandler.sendMessage(message);
                        }
                    });
                }

                String is_bind = data.get(position).get("is_bind").toString();
                if (is_bind.equals("2")) {
                    t_2.setVisibility(View.GONE);
                    t_5.setVisibility(View.VISIBLE);

                    String type = data.get(position).get("type").toString();
                    if (type.equals("42")) {
                        t_5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PubFunction.isConnect(activity)) {
                                    Intent intent = new Intent(activity, ScanCodeActivity_.class);
                                    activity.startActivityForResult(intent, 0x0005);
                                }
                                car_did = data.get(count).get("id").toString();
                            }
                        });
                    } else if (type.equals("32")) {
                        t_5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PubFunction.isConnect(activity)) {
                                    Intent intent = new Intent(activity, ScanCodeActivity_.class);
                                    activity.startActivityForResult(intent, 0x00010);
                                }
                                bar_id = data.get(count).get("id").toString();

                            }
                        });
                    } else if (type.equals("121") || type.equals("122")) {
                        t_5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (PubFunction.isConnect(activity)) {
                                    Intent intent = new Intent(activity, ScanCodeActivity_.class);
                                    activity.startActivityForResult(intent, 0x0006);
                                }
                                cha_id = data.get(count).get("id").toString();
                            }
                        });
                    }
                } else if (is_bind.equals("1")) {
                    t_5.setVisibility(View.GONE);
                    t_2.setVisibility(View.VISIBLE);
                }

                String fade_status = data.get(position).get("fade_status").toString();
                if (fade_status.equals("2")) {
                    t_2.setVisibility(View.GONE);
                    t_5.setVisibility(View.VISIBLE);
                    t_5.setText("确认解绑");
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Map<String, String> map = new HashMap<>();
                            map.put("id", data.get(count).get("id").toString());
                            map.put("type", data.get(count).get("shop_type").toString());
                            HttpIsSure_1(map);
                        }
                    });
                    t_5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Map<String, String> map = new HashMap<>();
                            map.put("id", data.get(count).get("id").toString());
                            map.put("type", data.get(count).get("shop_type").toString());
                            HttpIsSure_1(map);
                        }
                    });
                }
            } else if (view_type.equals("2")) {
                String type = data.get(position).get("type").toString();
                if (type.equals("10")) {
                    LayoutInflater inflater = LayoutInflater.from(activity);
                    view = inflater.inflate(resource[1], null);
                    ImageView imageView = view.findViewById(R.id.bottom_image);
//                    Picasso.with(activity).load(data.get(position).get("bg_img").toString()).into(imageView);
                    TextView t_1 = view.findViewById(R.id.t_1);
                    TextView t_2 = view.findViewById(R.id.t_2);
                    TextView t_3 = view.findViewById(R.id.t_3);
                    TextView t_4 = view.findViewById(R.id.t_4);
                    TextView t_8 = view.findViewById(R.id.t_8);
                    t_8.setVisibility(View.GONE);
                    t_1.setText(data.get(position).get("name").toString());
                    t_2.setText(data.get(position).get("rprice").toString());
                    t_3.setText("有效期：" + data.get(position).get("expire").toString());
                    t_4.setText(data.get(position).get("tips").toString());
                } else if (type.equals("20")) {
                    String mains = data.get(position).get("mains").toString();
                    if (mains.equals("[]")) {
                        LayoutInflater inflater = LayoutInflater.from(activity);
                        view = inflater.inflate(resource[1], null);
                        ImageView imageView = view.findViewById(R.id.bottom_image);
//                        Picasso.with(activity).load(data.get(position).get("bg_img").toString()).into(imageView);
                        TextView t_1 = view.findViewById(R.id.t_1);
                        TextView t_2 = view.findViewById(R.id.t_2);
                        TextView t_3 = view.findViewById(R.id.t_3);
                        TextView t_4 = view.findViewById(R.id.t_4);
                        TextView t_8 = view.findViewById(R.id.t_8);
                        t_8.setVisibility(View.GONE);
                        try {
                            t_1.setText(data.get(position).get("name").toString());
                            t_2.setText(data.get(position).get("rprice").toString());
                            t_3.setText(data.get(position).get("expire").toString());
                            t_4.setText(data.get(position).get("tips").toString());
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, MainShopOrder_.class);
                                intent.putExtra("id", data.get(count).get("id").toString());
                                intent.putExtra("rprice", data.get(count).get("rprice").toString());
                                activity.startActivity(intent);
                            }
                        });
                    } else {
                        LayoutInflater inflater = LayoutInflater.from(activity);
                        view = inflater.inflate(resource[2], null);
                        ImageView imageView = view.findViewById(R.id.bottom_image);
//                        Picasso.with(activity).load(data.get(position).get("bg_img").toString()).into(imageView);
                        TextView t_1 = view.findViewById(R.id.t_1);
                        TextView t_2 = view.findViewById(R.id.t_2);
                        TextView t_5_1 = view.findViewById(R.id.t_5_1);
                        TextView t_5_2 = view.findViewById(R.id.t_5_2);
                        TextView t_6_1 = view.findViewById(R.id.t_6_1);
                        TextView t_6_2 = view.findViewById(R.id.t_6_2);
                        TextView t_7_1 = view.findViewById(R.id.t_7_1);
                        TextView t_7_2 = view.findViewById(R.id.t_7_2);
                        TextView t_8 = view.findViewById(R.id.t_8);
                        ImageView i_6 = view.findViewById(R.id.i_6);
                        ImageView i_7 = view.findViewById(R.id.i_7);
                        try {
                            JSONTokener jsonTokener = new JSONTokener(data.get(position).get("mains").toString());
                            JSONArray packs_jsonObject_item_right_JsonArray = (JSONArray) jsonTokener.nextValue();

                            int packs_jsonObject_item_right_JsonArray_len = packs_jsonObject_item_right_JsonArray.length();
                            if (packs_jsonObject_item_right_JsonArray_len > 0) {
                                JSONObject jsonObject_item_1 = packs_jsonObject_item_right_JsonArray.getJSONObject(0);
                                t_5_1.setText(jsonObject_item_1.getString("name"));
                                t_5_2.setText(jsonObject_item_1.getString("rprice") + "元");
                            }
                            if (packs_jsonObject_item_right_JsonArray_len > 1) {
                                JSONObject jsonObject_item_2 = packs_jsonObject_item_right_JsonArray.getJSONObject(1);
                                t_6_1.setText(jsonObject_item_2.getString("name"));
                                t_6_2.setText(jsonObject_item_2.getString("rprice") + "元");
                                i_6.setVisibility(View.VISIBLE);
                            }
                            if (packs_jsonObject_item_right_JsonArray_len > 2) {
                                JSONObject jsonObject_item_3 = packs_jsonObject_item_right_JsonArray.getJSONObject(2);
                                t_7_1.setText(jsonObject_item_3.getString("name"));
                                t_7_2.setText(jsonObject_item_3.getString("rprice") + "元");
                                i_7.setVisibility(View.VISIBLE);
                            }

                            t_1.setText(data.get(position).get("name").toString());
                            t_2.setText(data.get(position).get("rprice").toString());
                            t_8.setText("有效期：" + data.get(position).get("expire").toString());

                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, MainShopOrder_.class);
                                intent.putExtra("id", data.get(count).get("id").toString());
                                intent.putExtra("rprice", data.get(count).get("rprice").toString());
                                activity.startActivity(intent);
                            }
                        });
                    }
                } else if (type.equals("30")) {
                    LayoutInflater inflater = LayoutInflater.from(activity);
                    view = inflater.inflate(resource[3], null);
                } else {//if (type.equals("232") || type.equals("231") || type.equals("230"))  这里表示3个年卡

                    LayoutInflater inflater = LayoutInflater.from(activity);
                    view = inflater.inflate(resource[1], null);

                    ImageView bottom_image = view.findViewById(R.id.bottom_image);
                    Picasso.with(context).load(data.get(position).get("bg_img").toString()).into(bottom_image);

                    TextView t_1 = view.findViewById(R.id.t_1);
                    t_1.setText(data.get(position).get("name").toString());

                    TextView t_2 = view.findViewById(R.id.t_2);
                    TextView t_9 = view.findViewById(R.id.t_9);
                    t_2.setText(data.get(position).get("rprice").toString());
                    t_9.setText("/年");

                    TextView t_3 = view.findViewById(R.id.t_3);
                    TextView t_4 = view.findViewById(R.id.t_4);
                    TextView t_8 = view.findViewById(R.id.t_8);
                    t_8.setVisibility(View.VISIBLE);
                    TextView t_10 = view.findViewById(R.id.t_10);

                    t_4.setText("如需再次换电可使用骑士币");

                    String days = data.get(position).get("days").toString();
                    String s = "有效期：" + data.get(position).get("end_ts").toString() + "    剩余<font color='#AA0A0A'><big>" + days + "</big></font>天";
                    t_8.setText(Html.fromHtml(s));

                    TextView t_12 = view.findViewById(R.id.t_12);
                    t_12.setVisibility(View.VISIBLE);
                    t_12.setText("注意：此卡仅限在本商家使用");
                    t_10.setText("再次购买");
                    ImageView imageview2 = view.findViewById(R.id.imageview2);
                    imageview2.setVisibility(View.VISIBLE);
                    ImageView imageview1 = view.findViewById(R.id.imageview1);
                    imageview1.setVisibility(View.VISIBLE);
                    //根据年卡的不同种类，添加“年字”图标
                    String expire = data.get(position).get("expire").toString();
                    if (expire.equals("0")) {// 0 未过期 1 已过期
                        String otype_str = data.get(position).get("type").toString();
                        if (otype_str.equals("230")) {
                            t_3.setText("使用说明：每2天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule1).into(imageview2);
                            Picasso.with(activity).load(R.mipmap.y299).into(imageview1);
                        } else if (otype_str.equals("231")) {
                            t_3.setText("使用说明：每1天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule1).into(imageview2);
                            Picasso.with(activity).load(R.mipmap.y499).into(imageview1);
                        } else {
                            t_3.setText("使用说明：每1天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule2).into(imageview2);
                            Picasso.with(activity).load(R.mipmap.y699).into(imageview1);
                        }
                        t_10.setVisibility(View.VISIBLE);

                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, MainShopOrder_.class);
                                intent.putExtra("id", data.get(count).get("id").toString());
                                intent.putExtra("rprice", data.get(count).get("rprice").toString());
                                activity.startActivity(intent);
                            }
                        });
                    } else {
                        String otype_str = data.get(position).get("type").toString();
                        if (otype_str.equals("230")) {
                            t_3.setText("使用说明：每2天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule1).into(imageview2);
                        } else if (otype_str.equals("231")) {
                            t_3.setText("使用说明：每1天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule1).into(imageview2);
                        } else {
                            t_3.setText("使用说明：每1天可免费换电 ");
                            Picasso.with(activity).load(R.mipmap.rule2).into(imageview2);
                        }
                        Picasso.with(activity).load(R.mipmap.expire_year).into(imageview1);
                        t_10.setVisibility(View.GONE);
                    }


                    //1次换电和2次换电图标
//                    String rule_str = data.get(position).get("rule").toString();
//                    if (rule_str.equals("1")) {
//                        Picasso.with(activity).load(R.mipmap.rule1).into(imageview2);
//                    } else {
//                        Picasso.with(activity).load(R.mipmap.rule2).into(imageview2);
//                    }

                    TextView t_11 = view.findViewById(R.id.t_11);
                    t_11.setVisibility(View.VISIBLE);

                }
            }
            return view;
        }
    }

}

