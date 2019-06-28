package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
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

/**
 * Created by hasee on 2017/6/6.
 */
@SuppressLint("Registered")
@EActivity(R.layout.main_message_1)
public class MainMessage_1 extends BaseActivity implements AbsListView.OnScrollListener {
    @ViewById
    LinearLayout page_return, none_panel;
    @ViewById
    ListView listview;
    @ViewById
    PullToRefreshLayout refresh;
    private List<Map<String, String>> dataList = new ArrayList<>();
    private MySimpleAdapter_message_1 mySimpleAdapter_message_1;
    private String lastid = "";
    private int count_page = 2;
    private int current_page = 1;

    @AfterViews
    void afterviews() {
        init();
    }

    @Click
    void page_return() {
        this.finish();
    }

    private void init() {
        refresh.setCanLoadMore(false);
        refresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                lastid = "";
                count_page = 2;
                current_page = 1;
                dataList.clear();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (PubFunction.isConnect(activity)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.show();
                                }
                            });
                            HttpMessage_1();
                        } else {
                            refresh.finishRefresh();
                        }
                    }
                }.start();
            }

            @Override
            public void loadMore() {
            }
        });
        listview.setOnScrollListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastid = "";
        count_page = 2;
        current_page = 1;
        dataList.clear();
        HttpMessage_1();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_page) {
                HttpMessage_1_RE(lastid);
                progressDialog.show();
                current_page = count_page;
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }

    /**
     * http接口：User/uOrder.html   获取消息信息
     */
    @Background
    void HttpMessage_1() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
//        Util.d("040201", "uid: " + uid);
        new OkHttpConnect(activity, PubFunction.app + "User/uOrder.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMessage_1(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpMessage_1(String response, String type) {
//        Util.d("040202", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
//                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    none_panel.setVisibility(View.GONE);
                    dataList.clear();
                    JSONObject jsonObject_data = jsonObject_response.getJSONObject("data");
                    JSONArray jSONArray = jsonObject_data.getJSONArray("lists");
                    this.lastid = jsonObject_data.getString("lastid");
                    if (jSONArray.equals("null") || jSONArray.equals("") || jSONArray.equals("[]")) {
                        none_panel.setVisibility(View.VISIBLE);
                    } else {
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jsonObject = jSONArray.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject.getString("id"));
                            map.put("gid", jsonObject.getString("gid"));
                            map.put("order_time", jsonObject.getString("order_time"));
                            map.put("type", jsonObject.getString("type"));
                            map.put("order_num", jsonObject.getString("order_num"));
                            map.put("order_title", jsonObject.getString("order_title"));
                            map.put("order_fee", jsonObject.getString("order_fee"));
                            map.put("order_status", jsonObject.getString("order_status"));
                            map.put("pay_time", jsonObject.getString("pay_time"));
                            map.put("auth", jsonObject.getString("auth"));
                            if (jsonObject.has("typer")) {
                                map.put("typer", jsonObject.getString("typer"));
                            } else {
                                map.put("typer", "线下支付");
                            }
                            map.put("status_desc", jsonObject.getString("status_desc"));
                            map.put("goods", jsonObject.getString("goods"));
                            dataList.add(map);
                        }
                        mySimpleAdapter_message_1 = new MySimpleAdapter_message_1(activity, dataList, R.layout.main_message_1_item);
                        listview.setAdapter(mySimpleAdapter_message_1);
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if (dataList.get(i).get("status_desc").equals("待支付")) {
                                    Intent intent = new Intent(activity, MainShopOrder_.class);
                                    intent.putExtra("id", dataList.get(i).get("gid"));
                                    intent.putExtra("order_num", dataList.get(i).get("order_num"));
                                    intent.putExtra("rprice", dataList.get(i).get("order_fee"));
                                    intent.putExtra("type", "aaaaa");
                                    activity.startActivity(intent);
//                            Intent intent = new Intent(activity, MainShopOrderV2_.class);
//                            intent.putExtra("order_num", data.get(a).get("order_num").toString());
//                            intent.putExtra("type", "10");
//                            activity.startActivity(intent);
                                }
                            }
                        });
                        listview.setDividerHeight(0);
                        refresh.finishRefresh();
                    }
                } else {
                    none_panel.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：User/uOrder.html    获取消息信息
     */
    @Background
    void HttpMessage_1_RE(String lastid_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("lastid", lastid_str));
        new OkHttpConnect(activity, PubFunction.app + "User/uOrder.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMessage_1_RE(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpMessage_1_RE(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONObject jsonObject_data = jsonObject_response.getJSONObject("data");
                    JSONArray jsonArray = jsonObject_data.getJSONArray("lists");
                    this.lastid = jsonObject_data.getString("lastid");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject.getString("id"));
                        map.put("gid", jsonObject.getString("gid"));
                        map.put("order_time", jsonObject.getString("order_time"));
                        map.put("type", jsonObject.getString("type"));
                        map.put("order_num", jsonObject.getString("order_num"));
                        map.put("order_title", jsonObject.getString("order_title"));
                        map.put("order_fee", jsonObject.getString("order_fee"));
                        map.put("order_status", jsonObject.getString("order_status"));
                        map.put("pay_time", jsonObject.getString("pay_time"));
                        map.put("auth", jsonObject.getString("auth"));
                        if (jsonObject.has("typer")) {
                            map.put("typer", jsonObject.getString("typer"));
                        } else {
                            map.put("typer", "线下支付");
                        }
                        map.put("status_desc", jsonObject.getString("status_desc"));
                        map.put("goods", jsonObject.getString("goods"));
                        dataList.add(map);
                    }
                    mySimpleAdapter_message_1.notifyDataSetChanged();
                    count_page = count_page + 1;
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    static class ViewHolder {
        TextView t_1;
        TextView t_2;
        TextView t_3;
        TextView t_4;
        TextView t_5;
        TextView t_6;
        TextView t_7;
        TextView t_8;
        TextView t_9;
        TextView t_10;
        TextView t_11;
        TextView t_12;
        ImageView i_m;
    }

    /**
     * 适配器
     */
    class MySimpleAdapter_message_1 extends BaseAdapter {
        private List<? extends Map<String, ?>> data;
        private int resource;

        MySimpleAdapter_message_1(Context context, List<? extends Map<String, ?>> data, int resource) {
            this.data = data;
            this.resource = resource;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = View.inflate(activity, resource, null);
                viewHolder = new ViewHolder();
                viewHolder.t_1 = (TextView) convertView.findViewById(R.id.t_1);
                viewHolder.t_2 = (TextView) convertView.findViewById(R.id.t_2);
                viewHolder.t_3 = (TextView) convertView.findViewById(R.id.t_3);
                viewHolder.t_4 = (TextView) convertView.findViewById(R.id.t_4);
                viewHolder.t_5 = (TextView) convertView.findViewById(R.id.t_5);
                viewHolder.t_6 = (TextView) convertView.findViewById(R.id.t_6);
                viewHolder.t_7 = (TextView) convertView.findViewById(R.id.t_7);
                viewHolder.t_8 = (TextView) convertView.findViewById(R.id.t_8);
                viewHolder.t_9 = (TextView) convertView.findViewById(R.id.t_9);
                viewHolder.t_10 = (TextView) convertView.findViewById(R.id.t_10);
                viewHolder.t_11 = (TextView) convertView.findViewById(R.id.t_11);
                viewHolder.t_12 = (TextView) convertView.findViewById(R.id.t_12);
                viewHolder.i_m = (ImageView) convertView.findViewById(R.id.i_m);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.t_1.setText("订单名称：" + data.get(position).get("order_title").toString());
            viewHolder.t_2.setText(data.get(position).get("order_time").toString());
            viewHolder.t_3.setText("订单号：" + data.get(position).get("order_num").toString());
            viewHolder.t_4.setText("支付方式：" + data.get(position).get("typer").toString());
            viewHolder.t_8.setText("合计：" + data.get(position).get("order_fee").toString() + "元");
            Picasso.with(activity).load(R.drawable.my_order_bg).into(viewHolder.i_m);
            if (data.get(position).get("type").toString().equals("100")) {
                String auth = data.get(position).get("auth").toString();
                if (auth.equals("2")) {
                    viewHolder.t_9.setText("未审核");
                    viewHolder.t_9.setTextColor(0xffe94b19);
                } else {
                    viewHolder.t_9.setText("已支付");
                    viewHolder.t_9.setTextColor(0xff0193e6);
                }
            } else {
                viewHolder.t_9.setText(data.get(position).get("status_desc").toString());
                if (data.get(position).get("status_desc").toString().equals("待支付")) {
                    viewHolder.t_9.setTextColor(0xffe94b19);
                } else {
                    viewHolder.t_9.setTextColor(0xff0193e6);
                }
            }
            String goods = data.get(position).get("goods").toString();
            if (!goods.equals("[]")) {
                try {
                    JSONTokener jsonTokener = new JSONTokener(goods);
                    JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                    if (jsonArray.length() > 0) {
                        JSONObject jsonObject_1 = jsonArray.getJSONObject(0);
                        String name_1 = jsonObject_1.getString("name");
                        String value_1 = jsonObject_1.getString("rprice");
                        viewHolder.t_10.setText(name_1);
                        viewHolder.t_5.setText(value_1 + "元");
                    }
                    if (jsonArray.length() > 1) {
                        JSONObject jsonObject_1 = jsonArray.getJSONObject(1);
                        String name_1 = jsonObject_1.getString("name");
                        String value_1 = jsonObject_1.getString("rprice");
                        viewHolder.t_11.setText(name_1);
                        viewHolder.t_6.setText(value_1 + "元");
                    }
                    if (jsonArray.length() > 2) {
                        JSONObject jsonObject_1 = jsonArray.getJSONObject(2);
                        String name_1 = jsonObject_1.getString("name");
                        String value_1 = jsonObject_1.getString("rprice");
                        viewHolder.t_12.setText(name_1);
                        viewHolder.t_7.setText(value_1 + "元");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return convertView;
        }
    }
}

