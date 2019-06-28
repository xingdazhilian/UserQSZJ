package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.hellohuandian.userqszj.pub.scanCode.ScanCodeActivity_;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
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

@EFragment(R.layout.main_shop_item_2)
public class MainShopItem_2 extends BaseFragment {

    public static Handler handler;
    @ViewById
    ListView listview;
    @ViewById
    LinearLayout none_panel;
    @ViewById
    PullToRefreshLayout refresh;
    @ViewById
    LinearLayout scan_panel;

    //    private View footview;
//    private int footview_count = 0;
    @ViewById
    TextView scan_panel_text, select_order;
    private List<Map<String, String>> datalist = new ArrayList<>();
    private View view;
    private String url = "";
    private String cid = "";
    private Handler changeDataHandler;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_shop_item_2, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @AfterViews
    void afterViews() {
        initParam();
        initHandler();

        url = getArguments().getString("url");
        cid = getArguments().getString("id");

        if (cid.equals("20")) {
            if (!MainShop.page_2_code.equals("")) {
                HttpGetShopInfoItem(url, MainShop.page_2_code, MainShop.mtoken);
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
                            HttpGetShopInfoItem(url, MainShop.page_2_code, MainShop.mtoken);
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

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        changeDataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//
//                String position_str = msg.getData().getString("position");
//                int position = Integer.parseInt(position_str);
//
//                String is_select_v = datalist.get(position).get("is_select");
//                if (is_select_v.equals("0")) {
//                    datalist.get(position).put("is_select", "1");
//                } else if (is_select_v.equals("1")) {
//                    datalist.get(position).put("is_select", "0");
//                }
//
//                int firstVisiblePosition = listview.getFirstVisiblePosition(); /**最后一个可见的位置**/
//                int lastVisiblePosition = listview.getLastVisiblePosition(); /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
//                if (position >= firstVisiblePosition && position <= lastVisiblePosition) { /**获取指定位置view对象**/
//                    View view = listview.getChildAt(position - firstVisiblePosition);
//                    ImageView i_2 = (ImageView) view.findViewById(R.id.i_3);
//                    String is_select = datalist.get(position).get("is_select").toString();
//                    if (is_select.equals("0")) {
//                        i_2.setImageResource(R.drawable.not_select_white);
//                    } else if (is_select.equals("1")) {
//                        i_2.setImageResource(R.drawable.is_select_white);
//                    }
//
//                }
//
//                int j = 0;
//                for (int i = 0; i < datalist.size(); i++) {
//                    String is_selected = datalist.get(i).get("is_select");
//                    if (is_selected.equals("1") || is_selected.equals("2")) {
//                        j = 1;
//                        break;
//                    }
//                }
//                if (j == 0) {
//                    select_order_panel.setVisibility(View.GONE);
//                    if (footview_count == 1) {
//                        listview.removeFooterView(footview);
//                        footview_count = 0;
//                    }
//                } else if (j == 1) {
//                    select_order_panel.setVisibility(View.VISIBLE);
//
//                    if (footview_count == 0) {
//                        footview = View.inflate(getActivity(), R.layout.main_shop_item_2_list_item_foot, null);
//                        listview.addFooterView(footview);
//                        footview_count = 1;
//                    }
//                }
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                HttpGetShopInfoItem(url, MainShop.page_2_code, MainShop.mtoken);
                scan_panel.setVisibility(View.GONE);
            }
        };
    }

    @Click
    void scan_panel_text() {
        Intent intent = new Intent(getActivity(), ScanCodeActivity_.class);
        getActivity().startActivityForResult(intent, 0x0005);
    }

//    @Click
//    void select_order() {
//        JSONArray jsonArray = new JSONArray();
//        for (int i = 0; i < datalist.size(); i++) {
//            JSONObject jsonObject = new JSONObject();
//            String is_selected = datalist.get(i).get("is_select");
//            if (is_selected.equals("1") || is_selected.equals("2")) {
//                String item_id = datalist.get(i).get("id");
//                String item_rprice = datalist.get(i).get("item_rprice");
//                try {
//                    jsonObject.put("gid",item_id);
//                    jsonObject.put("num","1");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                jsonArray.put(jsonObject);
//            }
//        }
//        Intent intent = new Intent(getActivity(), MainShopOrderV2_.class);
//        intent.putExtra("mjson", jsonArray.toString());
//        intent.putExtra("type", "30");
//        getActivity().startActivity(intent);
//    }

    @Background
    void HttpGetShopInfoItem(String path, String merid, String mtoken) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("merid", merid));
        dataList.add(new ParamTypeData("mtoken", mtoken));
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
//        Util.d("050501", "response:" + response);
        datalist.clear();
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
            scan_panel.setVisibility(View.VISIBLE);
            refresh.finishRefresh();
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                    JSONArray packs_JsonArray = jsonObject.getJSONArray("packs");
                    for (int i = 0; i < packs_JsonArray.length(); i++) {
                        JSONObject hello_Object_item = packs_JsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", hello_Object_item.getString("id"));
                        map.put("name", hello_Object_item.getString("name"));
                        map.put("rprice", hello_Object_item.getString("rprice"));
                        map.put("bg_img", hello_Object_item.getString("bg_img"));
                        map.put("packet", hello_Object_item.getString("packet"));
                        map.put("mains", hello_Object_item.getString("mains"));
                        map.put("sorts", hello_Object_item.getString("sorts"));
                        if (hello_Object_item.has("otype") && !hello_Object_item.get("otype").equals(JSONObject.NULL)) {
                            map.put("otype", hello_Object_item.getString("otype"));
                        }
                        if (hello_Object_item.has("is_buy") && !hello_Object_item.get("is_buy").equals(JSONObject.NULL)) {
                            map.put("is_buy", hello_Object_item.getString("is_buy"));
                        }
                        map.put("is_select", "0");
                        datalist.add(map);
                    }

                    MyGridAdapter myGridAdapter = new MyGridAdapter(getActivity(), datalist, R.layout.main_shop_item_2_list_item);
                    listview.setAdapter(myGridAdapter);
                    listview.setDividerHeight(0);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String item_id = datalist.get(position).get("id");
                            String is_buy = datalist.get(position).get("is_buy");
                            if (is_buy != null && is_buy.equals("0")) {
                                MyToast.showTheToast(context, "每个用户年卡过期前，仅限购买同类型年卡~");
                            } else {
//                            Intent intent = new Intent(getActivity(), MainShopOrderV2_.class);
//                            intent.putExtra("gid",item_id);
//                            intent.putExtra("type", "30");
//                            getActivity().startActivity(intent);
                                Intent intent = new Intent(getActivity(), MainShopOrder_.class);
                                intent.putExtra("id", item_id);
                                context.startActivity(intent);
                            }
                        }
                    });
                } else {
                    MyToast.showTheToast(getActivity(), msg);
                    scan_panel.setVisibility(View.VISIBLE);
                    refresh.finishRefresh();
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
                scan_panel.setVisibility(View.VISIBLE);
                refresh.finishRefresh();
            }
        }
    }


    class MyGridAdapter extends BaseAdapter {
        private Activity activity;
        private List<Map<String, String>> data;
        private int layout;

        MyGridAdapter(Activity activity, List<Map<String, String>> data, int layout) {
            this.activity = activity;
            this.data = data;
            this.layout = layout;
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
                convertView = View.inflate(activity, layout, null);
                viewHolder = new ViewHolder();
                viewHolder.t_1 = convertView.findViewById(R.id.t_1);
                viewHolder.t_2 = convertView.findViewById(R.id.t_2);
                viewHolder.t_5_1 = convertView.findViewById(R.id.t_5_1);
                viewHolder.t_5_2 = convertView.findViewById(R.id.t_5_2);
                viewHolder.t_6_1 = convertView.findViewById(R.id.t_6_1);
                viewHolder.t_6_2 = convertView.findViewById(R.id.t_6_2);
                viewHolder.t_7_1 = convertView.findViewById(R.id.t_7_1);
                viewHolder.t_7_2 = convertView.findViewById(R.id.t_7_2);
                viewHolder.i_6 = convertView.findViewById(R.id.i_6);
                viewHolder.i_7 = convertView.findViewById(R.id.i_7);
//                viewHolder.i_3 = (ImageView) convertView.findViewById(R.id.i_3);
                viewHolder.bottom_image = convertView.findViewById(R.id.bottom_image);
                viewHolder.bottom_devide_line = convertView.findViewById(R.id.bottom_devide_line);
                viewHolder.t_2_layout = convertView.findViewById(R.id.t_2_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (!data.get(position).get("bg_img").equals("")) {
                Picasso.with(activity).load(data.get(position).get("bg_img")).into(viewHolder.bottom_image);
            }
            //3种年卡
            String otype = data.get(position).get("otype");
            if (otype != null && (otype.equals("230") || otype.equals("231") || otype.equals("232"))) {
                viewHolder.t_2_layout.setVisibility(View.GONE);
                viewHolder.t_1.setVisibility(View.GONE);
                viewHolder.t_5_1.setVisibility(View.GONE);
                viewHolder.t_5_2.setVisibility(View.GONE);
            } else {
                viewHolder.t_2_layout.setVisibility(View.VISIBLE);
                viewHolder.t_1.setVisibility(View.VISIBLE);
                viewHolder.t_5_1.setVisibility(View.VISIBLE);
                viewHolder.t_5_2.setVisibility(View.VISIBLE);
            }
            viewHolder.t_1.setText(data.get(position).get("name"));
            viewHolder.t_2.setText(data.get(position).get("rprice"));
            if (position == data.size() - 1) {
                viewHolder.bottom_devide_line.setVisibility(View.VISIBLE);
            }
            try {
                JSONTokener jsonTokener = new JSONTokener(data.get(position).get("mains"));
                JSONArray packs_jsonObject_item_right_JsonArray = (JSONArray) jsonTokener.nextValue();
                viewHolder.i_6.setVisibility(View.GONE);
                viewHolder.i_7.setVisibility(View.GONE);
                viewHolder.t_6_1.setText("");
                viewHolder.t_6_2.setText("");
                viewHolder.t_7_1.setText("");
                viewHolder.t_7_2.setText("");
                int packs_jsonObject_item_right_JsonArray_len = packs_jsonObject_item_right_JsonArray.length();
                if (packs_jsonObject_item_right_JsonArray_len > 0) {
                    JSONObject jsonObject_item_1 = packs_jsonObject_item_right_JsonArray.getJSONObject(0);
                    viewHolder.t_5_1.setText(jsonObject_item_1.getString("name"));
                    viewHolder.t_5_2.setText(jsonObject_item_1.getString("rprice") + "元");
                }
                if (packs_jsonObject_item_right_JsonArray_len > 1) {
                    JSONObject jsonObject_item_2 = packs_jsonObject_item_right_JsonArray.getJSONObject(1);
                    viewHolder.t_6_1.setText(jsonObject_item_2.getString("name"));
                    viewHolder.t_6_2.setText(jsonObject_item_2.getString("rprice") + "元");
                    viewHolder.i_6.setVisibility(View.VISIBLE);
                }
                if (packs_jsonObject_item_right_JsonArray_len > 2) {
                    JSONObject jsonObject_item_3 = packs_jsonObject_item_right_JsonArray.getJSONObject(2);
                    viewHolder.t_7_1.setText(jsonObject_item_3.getString("name"));
                    viewHolder.t_7_2.setText(jsonObject_item_3.getString("rprice") + "元");
                    viewHolder.i_7.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            final String is_select = data.get(position).get("is_select").toString();
//            if (is_select.equals("0")) {
//                viewHolder.i_3.setImageResource(R.drawable.not_select_white);
//            } else if (is_select.equals("1")) {
//                viewHolder.i_3.setImageResource(R.drawable.is_select_white);
//            } else if (is_select.equals("2")) {
//                viewHolder.i_3.setImageResource(R.drawable.is_select_white);
//            }
            return convertView;
        }


        private class ViewHolder {
            TextView t_1;
            TextView t_2;
            TextView t_5_1;
            TextView t_5_2;
            TextView t_6_1;
            TextView t_6_2;
            TextView t_7_1;
            TextView t_7_2;
            ImageView i_6;
            ImageView i_7;
            //            ImageView i_3;
            ImageView bottom_image;
            ImageView bottom_devide_line;
            LinearLayout t_2_layout;
        }
    }


}

