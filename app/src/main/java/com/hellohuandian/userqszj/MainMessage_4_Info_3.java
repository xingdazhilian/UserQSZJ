package com.hellohuandian.userqszj;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
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
import com.wangjie.shadowviewhelper.ShadowProperty;
import com.wangjie.shadowviewhelper.ShadowViewDrawable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hellohuandian.userqszj.pub.PubFunction.dip2px;

/**
 * Created by hasee on 2017/6/6.
 */

@EActivity(R.layout.main_message_4_info)
public class MainMessage_4_Info_3 extends BaseActivity implements AbsListView.OnScrollListener {

    @ViewById
    LinearLayout page_return, none_panel;
    @ViewById
    TextView page_title;
    @ViewById
    ListView listview;
    @ViewById
    PullToRefreshLayout refresh;
    List<Map<String, String>> dataList = new ArrayList<>();
    private String tyoe = "";
    private MyBaseAdapter myBaseAdapter;
    private int count_page = 2;
    private int current_page = 1;


    @AfterViews
    void afterViews() {

        tyoe = getIntent().getStringExtra("type");
        page_title.setText(getIntent().getStringExtra("type_name"));
        listview.setOnScrollListener(this);

        refresh.setCanLoadMore(false);
        refresh.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {

                if (PubFunction.isConnect(activity)) {
                    count_page = 2;
                    current_page = 1;
                    dataList.clear();
                    HttpAdvicesLists(tyoe, 1);
                } else {
                    refresh.finishRefresh();
                }
            }

            @Override
            public void loadMore() {

            }
        });

    }

    @Click
    void page_return() {
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataList.clear();
        count_page = 2;
        current_page = 1;
        if (PubFunction.isConnect(activity)) {
            HttpAdvicesLists(tyoe, 1);
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if ((view.getLastVisiblePosition() == view.getCount() - 1) && current_page < count_page) {
                HttpAdvicesRELists(tyoe, count_page);
                progressDialog.show();
                current_page = count_page;
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    /**
     * http接口：Advices/type.html    获取消息信息
     */
    @Background
    void HttpAdvicesLists(String type, int page) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("type", type));
        dataList.add(new ParamTypeData("page", page + ""));
        new OkHttpConnect(activity, PubFunction.app + "Advices/Lists", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpAdvicesLists(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpAdvicesLists(String response, String type) {
        refresh.finishRefresh();
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    String data_str = jsonObject_response.getString("data");
                    if (data_str.equals("[]")) {
                        none_panel.setVisibility(View.VISIBLE);
                    } else {
                        none_panel.setVisibility(View.GONE);
                        JSONArray jsonArray = (JSONArray) jsonObject_response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", jsonObject.getString("id"));
                            map.put("title", jsonObject.getString("title"));
                            map.put("content", jsonObject.getString("content"));
                            map.put("create_time", jsonObject.getString("create_time"));
                            map.put("url", jsonObject.getString("url"));
                            map.put("images", jsonObject.getString("images"));
                            map.put("jwd", jsonObject.getString("jwd"));
                            map.put("is_read", jsonObject.getString("is_read"));
                            dataList.add(map);
                        }
                        myBaseAdapter = new MyBaseAdapter(activity, dataList, R.layout.main_message_4_info_item_1);
                        listview.setAdapter(myBaseAdapter);
                        listview.setDividerHeight(0);
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String image_str = dataList.get(position).get("images").toString();
                                try {
                                    if (image_str.equals("") || image_str.equals("[]")) {
                                        Intent intent = new Intent(activity, MainMessage_4_Content_.class);
                                        intent.putExtra("image", "");
                                        intent.putExtra("id", dataList.get(position).get("id").toString());
                                        intent.putExtra("t_1", dataList.get(position).get("title").toString());
                                        intent.putExtra("t_2", dataList.get(position).get("content").toString());
                                        activity.startActivity(intent);
                                    } else {
                                        JSONTokener jsonTokener = new JSONTokener(image_str);
                                        JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                                        String iamge_str = jsonArray.getString(0).toString();
                                        Intent intent = new Intent(activity, MainMessage_4_Content_.class);
                                        intent.putExtra("image", iamge_str);
                                        intent.putExtra("id", dataList.get(position).get("id").toString());
                                        intent.putExtra("t_1", dataList.get(position).get("title").toString());
                                        intent.putExtra("t_2", dataList.get(position).get("content").toString());
                                        activity.startActivity(intent);
                                    }
                                } catch (Exception e) {
                                    System.out.println(e.toString());
                                }
                            }
                        });
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
    void HttpAdvicesRELists(String type, int page) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("type", type));
        dataList.add(new ParamTypeData("page", page + ""));
        new OkHttpConnect(activity, PubFunction.app + "Advices/Lists", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpAdvicesRELists(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpAdvicesRELists(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    JSONArray jsonArray = (JSONArray) jsonObject_response.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", jsonObject.getString("id"));
                        map.put("title", jsonObject.getString("title"));
                        map.put("content", jsonObject.getString("content"));
                        map.put("create_time", jsonObject.getString("create_time"));
                        map.put("url", jsonObject.getString("url"));
                        map.put("images", jsonObject.getString("images"));
                        map.put("jwd", jsonObject.getString("jwd"));
                        map.put("is_read", jsonObject.getString("is_read"));
                        dataList.add(map);
                    }
                    myBaseAdapter.notifyDataSetChanged();
                    count_page = count_page + 1;
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    class MyBaseAdapter extends BaseAdapter {

        private Context context;
        private List<? extends Map<String, ?>> data;
        private int resource;

        public MyBaseAdapter(Context context, List<? extends Map<String, ?>> data, int resource) {
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
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(resource, null);

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.panel);
            ShadowProperty shadowProperty = new ShadowProperty()
                    .setShadowColor(0xff000000)
                    .setShadowDy(dip2px(activity, 0.5f))
                    .setShadowRadius(dip2px(activity, 7));
            ShadowViewDrawable sd = new ShadowViewDrawable(shadowProperty, Color.WHITE, 0, 0);
            ViewCompat.setBackground(linearLayout, sd);


            ImageView imageView = (ImageView) view.findViewById(R.id.i_1);
            ImageView line_1 = (ImageView) view.findViewById(R.id.line_1);

            if (data.get(position).get("images").toString().equals("[]")) {
                imageView.setVisibility(View.GONE);
                line_1.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                line_1.setVisibility(View.VISIBLE);
                String image_str = data.get(position).get("images").toString();
                try {

                    JSONTokener jsonTokener = new JSONTokener(image_str);
                    JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                    Picasso.with(activity).load(jsonArray.getString(0).toString()).into(imageView);

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

            TextView t_5 = (TextView) view.findViewById(R.id.t_5);
            if (!data.get(position).get("is_read").toString().equals("1")) {
                t_5.setText("未读");
                t_5.setTextColor(0xffe94b19);
            } else {
                t_5.setText("已读");
                t_5.setTextColor(0xffaaaaaa);
            }

            TextView t_1 = (TextView) view.findViewById(R.id.t_1);
            t_1.setText(data.get(position).get("create_time").toString());
            TextView t_2 = (TextView) view.findViewById(R.id.t_2);
            t_2.setText(data.get(position).get("title").toString());
            TextView t_3 = (TextView) view.findViewById(R.id.t_3);
            t_3.setText(data.get(position).get("content").toString());

            return view;
        }
    }
}