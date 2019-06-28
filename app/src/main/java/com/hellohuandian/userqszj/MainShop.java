package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.extend_plug.FragementPagerAdapter.MyFragmentPagerAdapter;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * modified by tianyanyu 20190325
 */
@SuppressLint("Registered")
@EActivity(R.layout.main_shop)
public class MainShop extends BaseFragmentActivity {
    //    private String[] sourceStrArray;
//    private int rent_bar_type = 0;
//    private int is_coupan = 0;
    public static String page_2_code = "";
    public static String page_3_code = "";
    public static String mtoken = "";
    @ViewById
    LinearLayout page_return, t_panel;
    @ViewById
    ViewPager mViewPager;
    @ViewById
    ImageView cursor;
    private String[] tab_str;
    private String[] tab_url;
    private String[] tab_id;
    private int[] colors = new int[]{0xff169d46, 0xff999999};
    private TextView[] textViews;
    private int[] positions;
    private int now_position = 0;
    //存放Fragment
    private ArrayList<Fragment> fragmentArrayList;
    //管理Fragment
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_shop);
        HttpGetShopInfo();
        progressDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        page_2_code = "";
        page_3_code = "";
        mtoken = "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传

        if (PubFunction.isConnect(activity)) {
            if (requestCode == 0x0005 && resultCode == RESULT_OK) {
                if (data != null) {
                    String content = data.getStringExtra("codedContent");
                    try {
                        String sourceStr = new String(Base64.decode(content.getBytes(), Base64.DEFAULT));
                        System.out.println(sourceStr);
                        int count = 0;
                        for (int i = 0; i < sourceStr.length(); i++) {
                            String s = String.valueOf(sourceStr.charAt(i)); //char 类型转String
                            if (s.equals("#")) {
                                count++;
                            }
                        }
                        if (count >= 4) {
                            String[] sourceStrArray = sourceStr.split("##");

                            if (PubFunction.isConnect(activity, false)) {
                                page_2_code = sourceStrArray[0];
                                mtoken = sourceStrArray[1];
                                MainShopItem_2.handler.sendMessage(new Message());

                            }
                        } else {
                            MyToast.showTheToast(activity, "请扫描正确的商家二维码！");
                        }
                    } catch (Exception e) {
                        MyToast.showTheToast(activity, "请扫描正确的商家二维码！");
                    }

                }
            }

            if (requestCode == 0x0010 && resultCode == RESULT_OK) {
                if (data != null) {
                    String content = data.getStringExtra("codedContent");
                    try {
                        String sourceStr = new String(Base64.decode(content.getBytes(), Base64.DEFAULT));
//                        Util.d("032702", sourceStr);
                        int count = 0;
                        for (int i = 0; i < sourceStr.length(); i++) {
                            String s = String.valueOf(sourceStr.charAt(i)); //char 类型转String
                            if (s.equals("#")) {
                                count++;
                            }
                        }
                        if (count >= 4) {
                            String[] sourceStrArray = sourceStr.split("##");
                            if (PubFunction.isConnect(activity, false)) {
                                page_3_code = sourceStrArray[0];
                                mtoken = sourceStrArray[1];
                                MainShopItem_3.handler.sendMessage(new Message());
                            }
                        } else {
                            MyToast.showTheToast(activity, "请扫描正确的商家二维码！");
                        }
                    } catch (Exception e) {
                        MyToast.showTheToast(activity, "请扫描正确的商家二维码！");
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString("id", "");
    }

    @Click
    void page_return() {
        this.finish();
    }


    /**
     * 初始化tab，创建3个textview横向放入t_panel中
     * 目前来看一共是3个tab：骑士币，购买，租聘
     */
    private void InitTextView() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;//获取屏幕宽度
        LinearLayout.LayoutParams params_t = (LinearLayout.LayoutParams) t_panel.getLayoutParams();
        params_t.height = PubFunction.dip2px(activity, 40);//将t_panel的高度设置为40dp
        params_t.width = width;//将t_panel的宽度设置为屏幕宽度
        t_panel.setLayoutParams(params_t);
        for (int i = 0; i < tab_str.length; i++) {//遍历服务器返回的tab名称
            TextView textView = new TextView(activity);//创建textview当做tab来用
            textView.setHeight(PubFunction.dip2px(activity, 40));//高40dp
            textView.setWidth(width / tab_str.length);//多个tab均分宽度
            textView.setText(tab_str[i]);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            textView.setOnClickListener(new MyOnClickListener(i));//点击事件实现了viewpager的切换
            if (i == 0) {
                textView.setTextColor(colors[0]);//第一个tab，也就是textview的文字颜色设置为橘色
            } else {
                textView.setTextColor(colors[1]);//其他设置为灰色
            }
            textViews[i] = textView;
            t_panel.addView(textView);//假设有3个tab，那么这3个tab就全部添加到t_panel中横向排列啦
        }
    }

    /**
     * 初始化页卡内容区
     */
    private void InitViewPager() {
        mViewPager.setAdapter(new MyFragmentPagerAdapter(fragmentManager, fragmentArrayList));
        //让ViewPager缓存2个页面
        mViewPager.setOffscreenPageLimit(2);
        //设置默认打开第一页
        mViewPager.setCurrentItem(0);
        //设置viewpager页面滑动监听事件
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }

    /**
     * 初始化动画
     */
    private void InitImageView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 获取分辨率宽度
        int screenW = dm.widthPixels;
        //动画图片宽度
        int bmpW = (screenW / tab_str.length);
        //设置动画图片cursor宽度，布局中暂时设置的是100dp，这里设置为1/3屏幕宽度
        setBmpW(cursor, bmpW);
        //初始化cursor移动的各个位置
        for (int i = 0; i < positions.length; i++) {
            if (i == 0) {
                positions[i] = 0;
            } else if (i == 1) {
                positions[i] = screenW / tab_str.length;
            } else {
                positions[i] = positions[1] * (i);
            }
        }
    }

    /**
     * 初始化Fragment，并添加到ArrayList中
     */
    private void InitFragment() {
        fragmentArrayList = new ArrayList<>();
        MainShopItem_1_ mainShopItem_1 = new MainShopItem_1_();
        Bundle bundle_1 = new Bundle();
        bundle_1.putString("url", tab_url[0]);
        bundle_1.putString("id", tab_id[0]);
        mainShopItem_1.setArguments(bundle_1);
        fragmentArrayList.add(mainShopItem_1);

        MainShopItem_2_ mainShopItem_2 = new MainShopItem_2_();
        Bundle bundle_2 = new Bundle();
        bundle_2.putString("url", tab_url[1]);
        bundle_2.putString("id", tab_id[1]);
        mainShopItem_2.setArguments(bundle_2);
        fragmentArrayList.add(mainShopItem_2);

        MainShopItem_3_ mainShopItem_3 = new MainShopItem_3_();
        Bundle bundle_3 = new Bundle();
        bundle_3.putString("url", tab_url[2]);
        bundle_3.putString("id", tab_id[2]);
        mainShopItem_3.setArguments(bundle_3);
        fragmentArrayList.add(mainShopItem_3);

        fragmentManager = getSupportFragmentManager();
    }

    /**
     * 设置动画图片宽度
     *
     * @param mWidth 需要设置的宽度
     */
    private void setBmpW(ImageView imageView, int mWidth) {
        ViewGroup.LayoutParams para;
        para = imageView.getLayoutParams();
        para.width = mWidth;
        imageView.setLayoutParams(para);
    }

    /**
     * http接口：Advices/type.html    获取消息信息
     */
    @Background
    void HttpGetShopInfo() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "Goods/hello.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetShopInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetShopInfo(String response, String type) {
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
                    JSONArray jsonArray = jsonObject.getJSONArray("category");
                    tab_str = new String[jsonArray.length()];
                    tab_url = new String[jsonArray.length()];
                    tab_id = new String[jsonArray.length()];
                    textViews = new TextView[jsonArray.length()];
                    positions = new int[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject_item = jsonArray.getJSONObject(i);
                        tab_str[i] = jsonObject_item.getString("name");
                        tab_url[i] = jsonObject_item.getString("urls");
//                        Util.d("050801", tab_url[i]);
                        tab_id[i] = jsonObject_item.getString("id");
                    }
                    //初始化tab，也就是3个textview
                    InitTextView();
                    //初始化cursor的宽度及cursor移动用到的位置数组
                    InitImageView();
                    //初始化Fragment，3个碎片页
                    InitFragment();
                    //初始化ViewPager
                    InitViewPager();
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    /**
     * tab点击监听
     *
     * @author weizhi
     * @version 1.0
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index;

        MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mViewPager.setCurrentItem(index);
        }
    }

    /**
     * 页卡切换监听
     *
     * @author weizhi
     * @version 1.0
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Animation animation;
            for (int i = 0; i < textViews.length; i++) {
                if (position == i) {
                    animation = new TranslateAnimation(now_position, positions[i], 0, 0);
                    now_position = positions[i];
                    textViews[i].setTextColor(colors[0]);
                    //当前页卡编号
                    animation.setFillAfter(true);// true:图片停在动画结束位置
                    animation.setDuration(300);
                    cursor.startAnimation(animation);
                } else {
                    textViews[i].setTextColor(colors[1]);
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
