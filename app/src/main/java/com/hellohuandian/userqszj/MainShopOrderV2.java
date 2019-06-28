package com.hellohuandian.userqszj;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.squareup.picasso.Picasso;

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

@EActivity(R.layout.main_shop_order_v2)
public class MainShopOrderV2 extends BaseActivity {

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView submit, price_1, title;
    @ViewById
    ListView listview, order_list;

    private String mjson = "";
    private String order_type = "";
    private String order_num = "";
    private String gid = "";
    private int pay_type = 1;
    private MyBaseAdapter myBaseAdapter;

    @AfterViews
    void AfterViews() {
        if (getIntent().hasExtra("type")) {
            order_type = getIntent().getStringExtra("type");
        }
        if (getIntent().hasExtra("mjson")) {
            mjson = getIntent().getStringExtra("mjson");
            HttpGetType(mjson, order_type);
        }
        if (getIntent().hasExtra("gid")) {
            gid = getIntent().getStringExtra("gid");
            HttpGetType_2(gid, order_type);
        }

        if (getIntent().hasExtra("order_num")) {
            order_num = getIntent().getStringExtra("order_num");
            HttpGetType_3(order_num, order_type);
        }

    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void submit() {
        if (order_type.equals("20")) {  // 我的钱包
            new MainPay(activity, pay_type + "", mjson, uid, 1, 1);
        } else if (order_type.equals("30")) {  //购买
            new MainPay(activity, pay_type + "", mjson, uid, 1, 1, 1);
        } else if (order_type.equals("40")) {   //租赁
            new MainPay(activity, pay_type + "", mjson, uid, 1, 1, 1, 1);
        } else if (order_type.equals("10")) {   //未支付订单
            new MainPay(activity, pay_type + "", order_num, uid, 1, 1, 1, 1, 1);
        } else if (order_type.equals("50")) {   //换电费
            new MainPay(activity, pay_type + "", gid, uid, 1, 1, 1, 1, 1, 1);
        }
    }


    @Background
    void HttpGetType(String mjson, String type) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("froms", type));
        dataList.add(new ParamTypeData("mjson", mjson));

        new OkHttpConnect(activity, PubFunction.api + "Pay/payerv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetType(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @Background
    void HttpGetType_2(String gid, String type) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("froms", type));
        dataList.add(new ParamTypeData("gid", gid));

        new OkHttpConnect(activity, PubFunction.api + "Pay/payerv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetType(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @Background
    void HttpGetType_3(String gid, String type) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("froms", type));
        dataList.add(new ParamTypeData("order_num", gid));

        new OkHttpConnect(activity, PubFunction.api + "Pay/payerv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetType(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetType(String response, String type) {
//        Util.d("042501", "response: " + response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONArray jsonObject_data = jsonObject_response.getJSONArray("data");
                    String rprice = jsonObject_response.getString("rprice");
                    price_1.setText(rprice + "");
                    submit.setText("确定支付  ¥ " + rprice + " 元");
                    final List<Map<String, String>> datalist = new ArrayList<>();
                    for (int i = 0; i < jsonObject_data.length(); i++) {
                        JSONObject jsonObject = jsonObject_data.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("type", jsonObject.getString("type"));
                        map.put("payer", jsonObject.getString("payer"));
                        if (jsonObject.has("img_url")) {
                            map.put("img_url", jsonObject.getString("img_url"));
                        } else {
                            map.put("img_url", "");
                        }
                        if (i == 0) {
                            pay_type = Integer.parseInt(jsonObject.getString("type"));
                        }
                        datalist.add(map);
                    }

                    myBaseAdapter = new MyBaseAdapter(activity, datalist, R.layout.main_shop_order_item_v2);
                    listview.setAdapter(myBaseAdapter);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            myBaseAdapter.set_select(position);
                            myBaseAdapter.notifyDataSetChanged();
                            pay_type = Integer.parseInt(datalist.get(position).get("type").toString());
                        }
                    });
                    PubFunction.setListViewHeight(listview);

                    JSONArray jsonArray_order = jsonObject_response.getJSONArray("lists");
                    JSONObject jsonObject_order = jsonArray_order.getJSONObject(0);
                    String title_str = jsonObject_order.getString("title");
                    title.setText(title_str);

                    final List<Map<String, String>> datalist_order = new ArrayList<>();
                    if (jsonObject_order.has("child") && !(jsonObject_order.get("child").equals(JSONObject.NULL))) {
                        JSONArray jsonArray_order_data = jsonObject_order.getJSONArray("child");
                        for (int i = 0; i < jsonArray_order_data.length(); i++) {
                            JSONObject jsonArray_order_data_item = jsonArray_order_data.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("otype", jsonArray_order_data_item.getString("otype"));
                            map.put("name", jsonArray_order_data_item.getString("name"));
                            map.put("fname", jsonArray_order_data_item.getString("fname"));
                            map.put("rprice", "¥ " + jsonArray_order_data_item.getString("rprice"));
                            map.put("num", "x" + jsonArray_order_data_item.getString("num"));
                            datalist_order.add(map);
                        }
                    }
                    SimpleAdapter simpleAdapter = new SimpleAdapter(activity, datalist_order, R.layout.main_shop_order_item_top_v2, new String[]{"fname", "rprice", "num"}, new int[]{R.id.t_1, R.id.t_2, R.id.t_3});
                    order_list.setAdapter(simpleAdapter);
                    order_list.setDividerHeight(0);
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    class MyBaseAdapter extends BaseAdapter {

        private Activity activity;
        private List<Map<String, String>> data;
        private int layout;

        private int is_select = 0;

        public MyBaseAdapter(Activity activity, List<Map<String, String>> data, int layout) {
            this.activity = activity;
            this.data = data;
            this.layout = layout;
        }


        public void set_select(int i) {
            this.is_select = i;
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


            MyBaseAdapter.ViewHolder viewHolder;
            if (null == convertView) {

                convertView = View.inflate(activity, layout, null);
                viewHolder = new MyBaseAdapter.ViewHolder();

                viewHolder.t_1 = (TextView) convertView.findViewById(R.id.t_1);
                viewHolder.i_2 = (ImageView) convertView.findViewById(R.id.i_2);
                viewHolder.i_1 = (ImageView) convertView.findViewById(R.id.i_1);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyBaseAdapter.ViewHolder) convertView.getTag();
            }

            if (!data.get(position).get("img_url").toString().equals("")) {
                Picasso.with(activity).load(data.get(position).get("img_url").toString()).into(viewHolder.i_1);
            }
            if (position == is_select) {
                Picasso.with(activity).load(R.drawable.zf3).into(viewHolder.i_2);
            } else {
                Picasso.with(activity).load(R.drawable.zf4).into(viewHolder.i_2);
            }
            viewHolder.t_1.setText(data.get(position).get("payer").toString());

            return convertView;
        }

        private class ViewHolder {
            TextView t_1;
            ImageView i_1;
            ImageView i_2;
        }
    }


}
