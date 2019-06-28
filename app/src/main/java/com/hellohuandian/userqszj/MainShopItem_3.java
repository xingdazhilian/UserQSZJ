package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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

@EFragment(R.layout.main_shop_item_3)
public class MainShopItem_3 extends BaseFragment {

    public static Handler handler;
    @ViewById
    ListView listview;
    @ViewById
    LinearLayout none_panel;
    @ViewById
    PullToRefreshLayout refresh;
    @ViewById
    LinearLayout scan_panel;
    @ViewById
    TextView scan_panel_text, select_order;
    private View view;
    private String url = "";
    private String cid = "";
    private List<Map<String, String>> datalist = new ArrayList<>();
    private Handler changeDataHandler;

    //    private View footview;
//    private int footview_count = 0;
    private String id_auth_tip = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_shop_item_3, container, false);
        return view;
    }

    @AfterViews
    void afterViews() {
        initParam();
        initHandler();
        url = getArguments().getString("url");
        cid = getArguments().getString("id");
        HttpGetUserInfo_2();
//        Util.d("032703", "url: " + url + ",cid: " + cid);

        if (cid.equals("30")) {
            if (!MainShop.page_3_code.equals("")) {
                HttpGetShopInfoItem(url, MainShop.page_3_code, MainShop.mtoken);
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
                            HttpGetShopInfoItem(url, MainShop.page_3_code, MainShop.mtoken);
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
//                    if(footview_count == 1){
//                        listview.removeFooterView(footview);
//                        footview_count = 0;
//                    }
//                } else if (j == 1) {
//                    select_order_panel.setVisibility(View.VISIBLE);
//
//                    if(footview_count == 0){
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
                HttpGetShopInfoItem(url, MainShop.page_3_code, MainShop.mtoken);
                scan_panel.setVisibility(View.GONE);
            }
        };
    }

    @Click
    void scan_panel_text() {
        Intent intent = new Intent(getActivity(), ScanCodeActivity_.class);
        getActivity().startActivityForResult(intent, 0x00010);
    }

//    @Click
//    void select_order(){
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
//        intent.putExtra("type", "40");
//        getActivity().startActivity(intent);
//    }


    @Background
    void HttpGetShopInfoItem(String path, String merid, String mtoken) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("merid", merid));
        dataList.add(new ParamTypeData("mtoken", mtoken));
//        Util.d("032705", "uid: " + uid + ",merid: " + merid + ",mtoken: " + mtoken);
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
        datalist.clear();
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(getActivity(), response);
            scan_panel.setVisibility(View.VISIBLE);
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
                        map.put("type", hello_Object_item.getString("type"));
                        map.put("rprice", hello_Object_item.getString("rprice"));
                        map.put("bg_img", hello_Object_item.getString("bg_img"));
                        map.put("packet", hello_Object_item.getString("packet"));
                        map.put("mains", hello_Object_item.getString("mains"));
                        map.put("is_select", "0");
                        datalist.add(map);
                    }

                    MyGridAdapter myGridAdapter = new MyGridAdapter(getActivity(), datalist, new int[]{R.layout.main_shop_item_3_list_item_1, R.layout.main_shop_item_3_list_item_2});
                    listview.setAdapter(myGridAdapter);
                    listview.setDividerHeight(0);

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            if (id_auth_tip.equals("已认证")) {
                                Intent intent = new Intent(getActivity(), MainShopOrder_.class);
                                intent.putExtra("id", datalist.get(position).get("id"));
                                getActivity().startActivity(intent);
//                                String item_id = datalist.get(position).get("id");
//                                Intent intent = new Intent(getActivity(), MainShopOrderV2_.class);
//                                intent.putExtra("gid",item_id);
//                                intent.putExtra("type", "40");
//                                getActivity().startActivity(intent);
                            } else {
                                LayoutInflater inflater = LayoutInflater.from(getActivity());
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                final AlertDialog mAlertDialog = builder.create();
                                View view = inflater.inflate(R.layout.alertdialog_is_auth, null);
                                TextView success_t = (TextView) view.findViewById(R.id.success);
                                success_t.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        getActivity().startActivity(new Intent(getActivity(), MainInfo_.class));
                                        getActivity().finish();
                                    }
                                });
                                TextView error_t = (TextView) view.findViewById(R.id.error);
                                error_t.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mAlertDialog.dismiss();
                                    }
                                });

                                mAlertDialog.show();
                                mAlertDialog.getWindow().setContentView(view);
                            }


                        }
                    });


                } else {
                    MyToast.showTheToast(getActivity(), msg);
                    scan_panel.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
                scan_panel.setVisibility(View.VISIBLE);
            }
        }
    }


    /**
     * http接口：User/personal.html   获取用户信息
     */
    @Background
    void HttpGetUserInfo_2() {

        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(getActivity(), PubFunction.app + "User/personal.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(getActivity(), uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetUserInfo_2(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();

    }

    @UiThread
    void onDataHttpGetUserInfo_2(String response, String type) {
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
                    id_auth_tip = jsonObject.getString("id_auth_tip");
                } else {
                    MyToast.showTheToast(getActivity(), msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(getActivity(), "JSON：" + e.toString());
            }
        }
    }

    class MyGridAdapter extends BaseAdapter {

        private Activity activity;
        private List<Map<String, String>> data;
        private int[] layout;

        public MyGridAdapter(Activity activity, List<Map<String, String>> data, int[] layout) {
            this.activity = activity;
            this.data = data;
            this.layout = layout;
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


            String type = data.get(position).get("type").toString();
            LayoutInflater inflater = LayoutInflater.from(activity);

            View view = null;
            if (type.equals("1")) {
                view = inflater.inflate(layout[0], null);
            } else {
                view = inflater.inflate(layout[1], null);
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.bottom_image);
            if (!data.get(position).get("bg_img").equals("")) {
                Picasso.with(activity).load(data.get(position).get("bg_img")).into(imageView);
            }
            TextView t_1 = (TextView) view.findViewById(R.id.t_1);
            t_1.setText(data.get(position).get("name").toString());
            TextView t_2 = (TextView) view.findViewById(R.id.t_2);
            t_2.setText(data.get(position).get("rprice").toString());


            TextView t_5_1 = (TextView) view.findViewById(R.id.t_5_1);
            TextView t_5_2 = (TextView) view.findViewById(R.id.t_5_2);
            TextView t_6_1 = (TextView) view.findViewById(R.id.t_6_1);
            TextView t_6_2 = (TextView) view.findViewById(R.id.t_6_2);
            TextView t_7_1 = (TextView) view.findViewById(R.id.t_7_1);
            TextView t_7_2 = (TextView) view.findViewById(R.id.t_7_2);
            ImageView i_6 = (ImageView) view.findViewById(R.id.i_6);
            ImageView i_7 = (ImageView) view.findViewById(R.id.i_7);
//            ImageView i_3 = (ImageView) view.findViewById(R.id.i_3);

            if (position == data.size() - 1) {
                ImageView bottom_devide_line = (ImageView) view.findViewById(R.id.bottom_devide_line);
                bottom_devide_line.setVisibility(View.VISIBLE);
            }

            try {
                JSONTokener jsonTokener = new JSONTokener(data.get(position).get("mains"));
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();


                if (type.equals("2")) {

                    JSONArray packs_jsonObject_item_left_JsonArray = jsonObject.getJSONArray("left");

                    TextView t_3 = (TextView) view.findViewById(R.id.t_3);
                    TextView t_4 = (TextView) view.findViewById(R.id.t_4);

                    int packs_jsonObject_item_left_JsonArray_len = packs_jsonObject_item_left_JsonArray.length();
                    if (packs_jsonObject_item_left_JsonArray_len == 1) {
                        t_3.setText(packs_jsonObject_item_left_JsonArray.getString(0));
                    }
                    if (packs_jsonObject_item_left_JsonArray_len == 2) {
                        t_3.setText(packs_jsonObject_item_left_JsonArray.getString(1));
                        t_4.setText(packs_jsonObject_item_left_JsonArray.getString(0));
                    }

                    JSONArray packs_jsonObject_item_right_JsonArray = jsonObject.getJSONArray("right");
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
                } else if (type.equals("1")) {
                    JSONArray packs_jsonObject_item_right_JsonArray = jsonObject.getJSONArray("left");
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
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            final String is_select = data.get(position).get("is_select").toString();
//            if (is_select.equals("0")) {
//                i_3.setImageResource(R.drawable.not_select_white);
//            } else if(is_select.equals("1")) {
//                i_3.setImageResource(R.drawable.is_select_white);
//            }  else if(is_select.equals("2")) {
//                i_3.setImageResource(R.drawable.is_select_white);
//            }


            return view;
        }
    }

}

