package com.hellohuandian.userqszj;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import org.androidannotations.annotations.EFragment;
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


@EFragment(R.layout.main_discount_1)
public class MainDiscount_1 extends BaseFragment implements AbsListView.OnScrollListener {


    public static Handler reloadHandler;
    @ViewById
    LinearLayout none_panel;
    @ViewById
    ListView listview;
    @ViewById
    PullToRefreshLayout refresh;
    private DiscountSimoleAdapter discountSimoleAdapter;
    private List<Map<String, String>> dataList = new ArrayList<>();
    private String lastid = "";
    private int count_page = 2;
    private int current_page = 1;

    @AfterViews
    void afterVoids() {
        initParam();
        init();
        main();
    }

    private void main() {
        if (PubFunction.isConnect(getActivity())) {
            progressDialog.show();
            HttpGetDis();
        }
    }

    private void init() {
        reloadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                lastid = "";
                count_page = 2;
                current_page = 1;

                if (PubFunction.isConnect(getActivity())) {
                    HttpGetDis();
                } else {
                    refresh.finishRefresh();
                }


            }
        };
        listview.setOnScrollListener(this);
        refresh.setCanLoadMore(false);
        refresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {

                lastid = "";
                count_page = 2;
                current_page = 1;

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (PubFunction.isConnect(getActivity())) {
                            progressDialog.show();
                            HttpGetDis();
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
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_page) {
                HttpGetDis_Re(lastid);
                progressDialog.show();
                current_page = count_page;
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    /**
     * http接口：Coupon/lists.html   除了第一页的数据
     */
    @Background
    void HttpGetDis_Re(String lastid_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("type", "1"));
        dataList.add(new ParamTypeData("lastid", lastid_str));

        new OkHttpConnect(getActivity(), PubFunction.app + "Coupon/lists.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(getActivity(), uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetDis_Re(response, type);
                progressDialog.dismiss();

            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetDis_Re(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {

                    JSONArray jSONArray = jsonObject_response.getJSONArray("data");
                    String lastid = jsonObject_response.getString("lastid");
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jsonObject = jSONArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject.getString("id"));
                        map.put("code", jsonObject.getString("code"));
                        map.put("urules", jsonObject.getString("urules"));
                        map.put("is_use", jsonObject.getString("is_use"));
                        map.put("expire", jsonObject.getString("expire"));
                        map.put("name", jsonObject.getString("name"));
                        map.put("title", jsonObject.getString("title"));
                        map.put("bg_img", jsonObject.getString("bg_img"));
                        map.put("unit", jsonObject.getString("unit"));
                        map.put("fval", jsonObject.getString("fval"));
                        dataList.add(map);
                    }
                    this.lastid = lastid;
                    discountSimoleAdapter.notifyDataSetChanged();
                    count_page = count_page + 1;
                } else {
                    MyToast.showTheToast(getActivity(), msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：Coupon/lists.html   第一页的数据
     */
    @Background
    void HttpGetDis() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("type", "1"));

        new OkHttpConnect(getActivity(), PubFunction.app + "Coupon/lists.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(getActivity(), uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetDis(response, type);
                progressDialog.dismiss();

            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetDis(String response, String type) {

        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    dataList.clear();
                    JSONArray jSONArray = jsonObject_response.getJSONArray("data");
                    String lastid = jsonObject_response.getString("lastid");
                    if (jSONArray.equals("null") || jSONArray.equals("") || jSONArray.equals("[]")) {
                        none_panel.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jsonObject = jSONArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject.getString("id"));
                        map.put("code", jsonObject.getString("code"));
                        map.put("urules", jsonObject.getString("urules"));
                        map.put("is_use", jsonObject.getString("is_use"));
                        map.put("expire", jsonObject.getString("expire"));
                        map.put("name", jsonObject.getString("name"));
                        map.put("title", jsonObject.getString("title"));
                        map.put("bg_img", jsonObject.getString("bg_img"));
                        map.put("unit", jsonObject.getString("unit"));
                        map.put("fval", jsonObject.getString("fval"));
                        dataList.add(map);
                    }
                    discountSimoleAdapter = new DiscountSimoleAdapter(getActivity(), dataList, R.layout.main_discount_item_1, new String[]{"a"}, new int[]{1});
                    listview.setAdapter(discountSimoleAdapter);
                    listview.setDividerHeight(0);

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String id_sre = dataList.get(position).get("code").toString();
                            if (PubFunction.isConnect(getActivity())) {
                                HttpDiscount(id_sre);
                                progressDialog.show();
                            }
                        }
                    });
                    this.lastid = lastid;
                    refresh.finishRefresh();
                } else {
                    none_panel.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
            }
        }
    }

    /**
     * http接口：Coupon/cashv2.html   使用优惠卷
     */
    @Background
    void HttpDiscount(String code_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("code", code_str));

        new OkHttpConnect(getActivity(), PubFunction.app + "Coupon/cashv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(getActivity(), uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpDiscount(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpDiscount(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                MyToast.showTheToast(getActivity(), msg);

                if (status.equals("1")) {
                    if (jsonObject_response.has("jump")) {
                        String data = jsonObject_response.getString("jump");
                        if (data.equals("1")) {
                            getActivity().startActivity(new Intent(getActivity(), MainWallet_.class));
                        } else {
                            MyToast.showTheToast(getActivity(), msg);
                        }
                    } else {
                        MyToast.showTheToast(getActivity(), msg);
                    }
                } else {
                    MyToast.showTheToast(getActivity(), msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
            }
        }
        MainDiscount_1.reloadHandler.sendMessage(new Message());
        MainDiscount_2.reloadHandler.sendMessage(new Message());
    }


    /**
     * 适配器
     */
    class DiscountSimoleAdapter extends SimpleAdapter {

        private Context context;
        private List<? extends Map<String, ?>> data;
        private int resource;

        public DiscountSimoleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.context = context;
            this.data = data;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (null == convertView) {

                convertView = View.inflate(context, resource, null);
                viewHolder = new ViewHolder();
                viewHolder.t_1 = (TextView) convertView.findViewById(R.id.t_1);
                viewHolder.t_2 = (TextView) convertView.findViewById(R.id.t_2);
                viewHolder.t_3 = (TextView) convertView.findViewById(R.id.t_3);
                viewHolder.t_4 = (TextView) convertView.findViewById(R.id.t_4);
                viewHolder.t_5 = (TextView) convertView.findViewById(R.id.is_use);
                viewHolder.i_1 = (ImageView) convertView.findViewById(R.id.i_1);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.t_1.setText(data.get(position).get("name").toString());
            if (data.get(position).get("unit").toString().equals("")) {
                viewHolder.t_2.setText(data.get(position).get("fval").toString() + "元");
            } else {
                viewHolder.t_2.setText(data.get(position).get("fval").toString() + data.get(position).get("unit").toString());
            }
            viewHolder.t_3.setText(data.get(position).get("expire").toString());
            viewHolder.t_4.setText(data.get(position).get("urules").toString());

            String is_use_str = data.get(position).get("is_use").toString();
            if (is_use_str.equals("-1")) {
                viewHolder.t_5.setVisibility(View.GONE);
                viewHolder.t_2.setTextColor(0xffaaaaaa);
            } else {
                viewHolder.t_5.setVisibility(View.VISIBLE);
            }
            Picasso.with(getActivity()).load(data.get(position).get("bg_img").toString()).into(viewHolder.i_1);
            return convertView;
        }

        private class ViewHolder {
            TextView t_1;
            TextView t_2;
            TextView t_3;
            TextView t_4;
            TextView t_5;
            ImageView i_1;
        }
    }


}


