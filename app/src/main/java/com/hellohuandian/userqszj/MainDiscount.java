package com.hellohuandian.userqszj;

import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.extend_plug.FragementPagerAdapter.MyFragmentPagerAdapter;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasee on 2017/6/6.
 */


@EActivity(R.layout.main_discount)
public class MainDiscount extends BaseFragmentActivity {


    @ViewById
    LinearLayout page_return, none_panel, t_panel;
    @ViewById
    TextView submit;
    @ViewById
    EditText number;

    @ViewById
    ViewPager mViewPager;
    @ViewById
    ImageView cursor;

    private int[] colors = new int[]{0xff169d46, 0xff999999};
    private TextView[] textViews = new TextView[colors.length];
    private int[] positions = new int[colors.length];
    private int now_position = 0;


    private String[] tab_str = new String[]{"未使用", "已使用"};

    //动画图片宽度
    private int bmpW;
    //当前页卡编号
    private int currIndex = 0;
    //存放Fragment
    private ArrayList<Fragment> fragmentArrayList;
    //管理Fragment
    private FragmentManager fragmentManager;
    private String[] sourceStrArray;
    private int rent_bar_type = 0;
    private int is_coupan = 0;

    @AfterViews
    void afterVoids() {
        //初始化TextView
        InitTextView();
        //初始化ImageView
        InitImageView();
        //初始化Fragment
        InitFragment();
        //初始化ViewPager
        InitViewPager();
    }


    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void submit() {

        if (number.getText().toString().equals("")) {
            MyToast.showTheToast(activity, "请输入您的优惠码！");
        } else {
            HttpDiscount(number.getText().toString());
            number.setText("");
        }
    }

    /**
     * 初始化头标
     */
    private void InitTextView() {

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        LinearLayout.LayoutParams params_t = (LinearLayout.LayoutParams) t_panel.getLayoutParams();
        params_t.height = PubFunction.dip2px(activity, 40);
        params_t.width = width;
        t_panel.setLayoutParams(params_t);


        for (int i = 0; i < tab_str.length; i++) {
            TextView textView = new TextView(activity);
            textView.setHeight(PubFunction.dip2px(activity, 40));
            textView.setWidth(width / tab_str.length);

            textView.setText(tab_str[i]);
            textView.setBackgroundColor(0xfff2f2f2);
            textView.setGravity(Gravity.CENTER);//居中
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
            textView.setOnClickListener(new MyOnClickListener(i));

            if (i == 0) {
                textView.setTextColor(colors[0]);
            } else {
                textView.setTextColor(colors[1]);
            }

            textViews[i] = textView;
            t_panel.addView(textView);
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
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    /**
     * 初始化动画
     */
    private void InitImageView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // 获取分辨率宽度
        int screenW = dm.widthPixels;
        bmpW = (screenW / tab_str.length);
        //设置动画图片宽度
        setBmpW(cursor, bmpW);


        for (int i = 0; i < positions.length; i++) {

            if (i == 0) {
                positions[i] = 0;
            } else if (i == 1) {
                positions[i] = (int) (screenW / tab_str.length);
            } else {
                positions[i] = positions[1] * (i);
            }

        }
    }

    /**
     * 初始化Fragment，并添加到ArrayList中
     */
    private void InitFragment() {
        fragmentArrayList = new ArrayList<Fragment>();

        MainDiscount_1_ MainDiscount_1 = new MainDiscount_1_();
        fragmentArrayList.add(MainDiscount_1);

        MainDiscount_2_ MainDiscount_2 = new MainDiscount_2_();
        fragmentArrayList.add(MainDiscount_2);

        fragmentManager = getSupportFragmentManager();

    }

    /**
     * 设置动画图片宽度
     *
     * @param mWidth
     */
    private void setBmpW(ImageView imageView, int mWidth) {
        ViewGroup.LayoutParams para;
        para = imageView.getLayoutParams();
        para.width = mWidth;
        imageView.setLayoutParams(para);
    }

    /**
     * http接口：Coupon/cashv2.html   使用优惠卷
     */
    @Background
    void HttpDiscount(String code_str) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("code", code_str));
        new OkHttpConnect(activity, PubFunction.app + "Coupon/cashv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpDiscount(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpDiscount(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                MyToast.showTheToast(activity, msg);
                MainDiscount_1.reloadHandler.sendMessage(new Message());
                MainDiscount_2.reloadHandler.sendMessage(new Message());
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
        MainDiscount_1.reloadHandler.sendMessage(new Message());
        MainDiscount_2.reloadHandler.sendMessage(new Message());
    }

    /**
     * 头标点击监听
     *
     * @author weizhi
     * @version 1.0
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
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

            Animation animation = null;

            for (int i = 0; i < textViews.length; i++) {

                if (position == i) {
                    animation = new TranslateAnimation(now_position, positions[i], 0, 0);
                    now_position = positions[i];
                    textViews[i].setTextColor(colors[0]);

                } else {
                    textViews[i].setTextColor(colors[1]);
                }
            }

            currIndex = position;

            animation.setFillAfter(true);// true:图片停在动画结束位置
            animation.setDuration(300);
            cursor.startAnimation(animation);

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}


