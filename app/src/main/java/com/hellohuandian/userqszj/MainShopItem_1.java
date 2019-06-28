package com.hellohuandian.userqszj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
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
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EFragment(R.layout.main_shop_item_1)
public class MainShopItem_1 extends BaseFragment {
    @ViewById
    ListView listview;
    @ViewById
    GridView gridview;
    @ViewById
    LinearLayout none_panel, bottom_panel;
    @ViewById
    PullToRefreshLayout refresh;
    @ViewById
    ImageView bottom_image;
    @ViewById
    TextView t_1, t_2, t_3, t_4;
    @ViewById
    TextView t_5_1, t_5_2, t_6_1, t_6_2, t_7_1, t_7_2;
    @ViewById
    ImageView i_6, i_7;
    private String url = "";
    private MyGridAdapter myGridAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_shop_item_1, container, false);
    }

    @AfterViews
    void afterViews() {
        initParam();
        url = getArguments().getString("url");
        String cid = getArguments().getString("id");
        if (cid != null && cid.equals("10")) {
            if (PubFunction.isConnect(getActivity())) {
                HttpGetShopInfoItem(url);
            }
        }
        refresh.setCanLoadMore(false);
        refresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
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
                            HttpGetShopInfoItem(url);
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

    @Background
    void HttpGetShopInfoItem(String path) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(getActivity(), path, dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(getActivity(), uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetShopInfoItem(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetShopInfoItem(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                    //top数据
                    JSONArray hello_JsonArray = jsonObject.getJSONArray("hello");
                    final List<Map<String, String>> gridListData = new ArrayList<>();
                    for (int i = 0; i < hello_JsonArray.length(); i++) {
                        JSONObject hello_Object_item = hello_JsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", hello_Object_item.getString("id"));
                        map.put("rprice", hello_Object_item.getString("rprice"));
                        map.put("oprice", hello_Object_item.getString("oprice"));
                        map.put("fvalue", hello_Object_item.getString("fvalue"));
                        gridListData.add(map);
                    }
                    if (myGridAdapter == null) {
                        myGridAdapter = new MyGridAdapter(getActivity(), gridListData, R.layout.main_shop_item_1_grid_item);
                        gridview.setAdapter(myGridAdapter);
                    } else {
                        myGridAdapter.notifyDataSetChanged();
                    }
                    PubFunction.setGridViewHeight(gridview, hello_JsonArray.length(), 2);
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            Intent intent = new Intent(getActivity() , MainShopOrderV2_.class);
//                            intent.putExtra("gid",gridListData.get(position).get("id").toString());
//                            intent.putExtra("type","50");
//                            getActivity().startActivity(intent);
                            String item_id = gridListData.get(position).get("id");
                            Intent intent = new Intent(getActivity(), MainShopOrder_.class);
                            intent.putExtra("id", item_id);
                            getActivity().startActivity(intent);
                        }
                    });
                    //bottom数据
                    if (!jsonObject.getString("packs").equals("[]")) {
                        JSONObject packs_jsonObject = jsonObject.getJSONObject("packs");
                        final String id = packs_jsonObject.getString("id");
                        String name = packs_jsonObject.getString("name");
                        final String rprice = packs_jsonObject.getString("rprice");
//                        String oprice = packs_jsonObject.getString("oprice");
                        JSONObject packs_jsonObject_item = packs_jsonObject.getJSONObject("descs");
                        String bottom_image_str = packs_jsonObject_item.getString("bg_img");
                        JSONArray packs_jsonObject_item_left_JsonArray = packs_jsonObject_item.getJSONArray("left");
                        JSONArray packs_jsonObject_item_right_JsonArray = packs_jsonObject_item.getJSONArray("right");
                        Picasso.with(getActivity()).load(bottom_image_str).into(bottom_image);
                        t_1.setText(name);
                        t_2.setText(rprice);
                        int packs_jsonObject_item_left_JsonArray_len = packs_jsonObject_item_left_JsonArray.length();
                        if (packs_jsonObject_item_left_JsonArray_len == 1) {
                            t_3.setText(packs_jsonObject_item_left_JsonArray.getString(0));
                        }
                        if (packs_jsonObject_item_left_JsonArray_len == 2) {
                            t_3.setText(packs_jsonObject_item_left_JsonArray.getString(1));
                            t_4.setText(packs_jsonObject_item_left_JsonArray.getString(0));
                        }
                        int packs_jsonObject_item_right_JsonArray_len = packs_jsonObject_item_right_JsonArray.length();
                        if (packs_jsonObject_item_right_JsonArray_len > 0) {
                            JSONObject jsonObject_item_1 = packs_jsonObject_item_right_JsonArray.getJSONObject(0);
                            t_5_1.setText(jsonObject_item_1.getString("title"));
                            t_5_2.setText(jsonObject_item_1.getString("price") + "元");
                        }
                        if (packs_jsonObject_item_right_JsonArray_len > 1) {
                            JSONObject jsonObject_item_2 = packs_jsonObject_item_right_JsonArray.getJSONObject(1);
                            t_6_1.setText(jsonObject_item_2.getString("title"));
                            t_6_2.setText(jsonObject_item_2.getString("price") + "元");
                            i_6.setVisibility(View.VISIBLE);
                        }
                        if (packs_jsonObject_item_right_JsonArray_len > 2) {
                            JSONObject jsonObject_item_3 = packs_jsonObject_item_right_JsonArray.getJSONObject(2);
                            t_7_1.setText(jsonObject_item_3.getString("title"));
                            t_7_2.setText(jsonObject_item_3.getString("price") + "元");
                            i_7.setVisibility(View.VISIBLE);
                        }
                        bottom_panel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                Intent intent = new Intent(getActivity() , MainShopOrderV2_.class);
//                                intent.putExtra("gid",id);
//                                intent.putExtra("type","50");
//                                getActivity().startActivity(intent);

                                Intent intent = new Intent(getActivity(), MainShopOrder_.class);
                                intent.putExtra("id", id);
                                getActivity().startActivity(intent);
                            }
                        });
                    }
                } else {
                    MyToast.showTheToast(getActivity(), msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
            }
        }
    }

    static class ViewHolder {
        TextView t_1;
        TextView t_2;
        TextView t_3;
    }

    class MyGridAdapter extends BaseAdapter {
        private List<Map<String, String>> data;
        private int layout;
        private LayoutInflater inflater;

        MyGridAdapter(Activity activity, List<Map<String, String>> data, int layout) {
            this.data = data;
            this.layout = layout;
            inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public Map<String, String> getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Map<String, String> item = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(layout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.t_1 = (TextView) convertView.findViewById(R.id.t_1);
                viewHolder.t_2 = (TextView) convertView.findViewById(R.id.t_2);
                viewHolder.t_3 = (TextView) convertView.findViewById(R.id.t_3);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.t_1.setText(item.get("fvalue") + "币");
            viewHolder.t_2.setText(item.get("rprice") + "元");
            viewHolder.t_3.setText(item.get("oprice") + "元");
            return convertView;
        }
    }
}

