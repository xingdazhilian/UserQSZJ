package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

@SuppressLint("Registered")
@EActivity(R.layout.main_shop_order)
public class MainShopOrder extends BaseActivity {
    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView submit, price_1;
    @ViewById
    ListView listview;
    private int pay_type = 1;
    private String id = "";
    private String type = "";
    private String order_num = "";
    private MyBaseAdapter myBaseAdapter;
    private long mLastClickTime = 0;

    @AfterViews
    void afterViews() {
        id = getIntent().getStringExtra("id");
        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
            order_num = getIntent().getStringExtra("order_num");
            HttpGetType_ordernum(order_num);
            System.out.println("订单支付");
        } else {
            System.out.println("商品支付");
            HttpGetType(id);
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void submit() {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > 1000L) {
            if (type.equals("")) {
                new MainPay(activity, pay_type + "", id, uid);
                System.out.println("type_1");
            } else {
                new MainPay(activity, pay_type + "", order_num, uid, 1);
                System.out.println("type_2");
            }
            mLastClickTime = nowTime;
        } else {
            Toast.makeText(activity, "请勿重复点击", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * http接口：Pay/payer.html   获取消息信息
     */
    @Background
    void HttpGetType(String gid) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("gid", gid));
//        Util.d("033003", "uid: " + uid + ", gid: " + gid);
        new OkHttpConnect(activity, PubFunction.api + "Pay/payer.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetType(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @Background
    void HttpGetType_ordernum(String order_num) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("order_num", order_num));
        new OkHttpConnect(activity, PubFunction.api + "Pay/payer.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetType(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetType(String response, String type) {
//        Util.d("032708", response);
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
                    price_1.setText(rprice);
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

                    myBaseAdapter = new MyBaseAdapter(activity, datalist, R.layout.main_shop_order_item);
                    listview.setAdapter(myBaseAdapter);
                    listview.setDividerHeight(0);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            myBaseAdapter.set_select(position);
                            myBaseAdapter.notifyDataSetChanged();
                            pay_type = Integer.parseInt(datalist.get(position).get("type"));
                        }
                    });
                    PubFunction.setListViewHeight(listview);
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
        ImageView i_1;
        ImageView i_2;
    }

    class MyBaseAdapter extends BaseAdapter {
        private Activity activity;
        private List<Map<String, String>> data;
        private int layout;
        private int is_select = 0;

        MyBaseAdapter(Activity activity, List<Map<String, String>> data, int layout) {
            this.activity = activity;
            this.data = data;
            this.layout = layout;
        }

        void set_select(int i) {
            this.is_select = i;
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
                viewHolder.t_1 = (TextView) convertView.findViewById(R.id.t_1);
                viewHolder.i_2 = (ImageView) convertView.findViewById(R.id.i_2);
                viewHolder.i_1 = (ImageView) convertView.findViewById(R.id.i_1);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (!data.get(position).get("img_url").equals("")) {
                Picasso.with(activity).load(data.get(position).get("img_url")).into(viewHolder.i_1);
            }
            if (position == is_select) {
                Picasso.with(activity).load(R.drawable.zf3).into(viewHolder.i_2);
            } else {
                Picasso.with(activity).load(R.drawable.zf4).into(viewHolder.i_2);
            }
            viewHolder.t_1.setText(data.get(position).get("payer"));
            return convertView;
        }
    }
}
