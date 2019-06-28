package com.hellohuandian.userqszj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_bar_health_list)
public class MainBarHealthList extends BaseActivity implements AbsListView.OnScrollListener {


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

                if (PubFunction.isConnect(activity)) {
                    progressDialog.show();
                    HttpMessage_1();
                } else {
                    refresh.finishRefresh();
                }
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

        if (PubFunction.isConnect(activity)) {
            progressDialog.show();
            HttpMessage_1();
        }
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
     * http接口：Advices/type.html    获取消息信息
     */
    @Background
    void HttpMessage_1() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "User/lifeLists", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMessage_1(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpMessage_1(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    none_panel.setVisibility(View.GONE);
                    JSONObject jsonObject_data = jsonObject_response.getJSONObject("data");

                    JSONArray jSONArray = jsonObject_data.getJSONArray("lists");
                    String lastid = jsonObject_data.getString("lastid");
                    this.lastid = lastid;
                    if (jSONArray.equals("null") || jSONArray.equals("") || jSONArray.equals("[]")) {
                        none_panel.setVisibility(View.VISIBLE);
                    } else {

                        for (int i = 0; i < jSONArray.length(); i++) {

                            JSONObject jsonObject = jSONArray.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject.getString("id"));
                            map.put("name", jsonObject.getString("name"));
                            map.put("number", jsonObject.getString("number"));
                            map.put("in_electric", jsonObject.getString("in_electric"));
                            map.put("out_electric", jsonObject.getString("out_electric"));
                            map.put("electric", jsonObject.getString("electric"));
                            map.put("cycles", jsonObject.getString("cycles"));
                            map.put("tips", jsonObject.getString("tips"));
                            map.put("create_time", jsonObject.getString("create_time"));

                            dataList.add(map);
                        }
                        mySimpleAdapter_message_1 = new MySimpleAdapter_message_1(activity, dataList, R.layout.main_bar_health_list_item);
                        listview.setAdapter(mySimpleAdapter_message_1);
                        listview.setDividerHeight(0);
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：Advices/type.html    获取消息信息
     */
    @Background
    void HttpMessage_1_RE(String lastid_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("lastid", lastid_str));
        new OkHttpConnect(activity, PubFunction.app + "User/lifeLists", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMessage_1_RE(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpMessage_1_RE(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    JSONObject jsonObject_data = jsonObject_response.getJSONObject("data");
                    JSONArray jsonArray = jsonObject_data.getJSONArray("lists");
                    String lastid = jsonObject_data.getString("lastid");
                    this.lastid = lastid;

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject.getString("id"));
                        map.put("name", jsonObject.getString("name"));
                        map.put("number", jsonObject.getString("number"));
                        map.put("in_electric", jsonObject.getString("in_electric"));
                        map.put("out_electric", jsonObject.getString("out_electric"));
                        map.put("electric", jsonObject.getString("electric"));
                        map.put("cycles", jsonObject.getString("cycles"));
                        map.put("tips", jsonObject.getString("tips"));
                        map.put("create_time", jsonObject.getString("create_time"));

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

    class MySimpleAdapter_message_1 extends BaseAdapter {

        private Context context;
        private List<? extends Map<String, ?>> data;
        private int resource;

        public MySimpleAdapter_message_1(Context context, List<? extends Map<String, ?>> data, int resource) {

            this.context = context;
            this.data = data;
            this.resource = resource;
        }

        @Override
        public int getCount() {
            return data.size();
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

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(resource, null);

            TextView t_1 = (TextView) view.findViewById(R.id.t_1);
            TextView t_2 = (TextView) view.findViewById(R.id.t_2);
            TextView t_3 = (TextView) view.findViewById(R.id.t_3);
            TextView t_4 = (TextView) view.findViewById(R.id.t_4);

            TextView t_5 = (TextView) view.findViewById(R.id.t_5);

            t_1.setText("站点：" + data.get(position).get("name").toString());
            t_2.setText("ID:" + data.get(position).get("number").toString());
            t_3.setText(data.get(position).get("create_time").toString());
            t_4.setText(data.get(position).get("tips").toString());
            t_5.setText(data.get(position).get("cycles").toString() + " 循环");

            return view;
        }
    }
}

