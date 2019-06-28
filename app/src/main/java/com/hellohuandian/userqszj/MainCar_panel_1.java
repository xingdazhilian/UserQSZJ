package com.hellohuandian.userqszj;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.gaode.Constants;
import com.tandong.bottomview.view.BottomView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

import static com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap;

@EActivity(R.layout.main_car_panel_1)
public class MainCar_panel_1 extends BaseActivity implements LocationSource, AMapLocationListener {
    @ViewById
    LinearLayout page_return, select_panel;
    @ViewById
    TextView select_text;
    @ViewById
    MapView map_view;
    @ViewById
    FrameLayout main_panel;
    String[] dataString = new String[]{"1km", "3km", "5km", "10km", "15km", "20km", "30km"};
    int[] dataInt = new int[]{1000, 3000, 5000, 10000, 15000, 20000, 30000};
    private Bundle savedInstanceState;
    private AMap aMap;
    private UiSettings mUiSettings;
    private GeocodeSearch geocoderSearch;
    private MyLocationStyle myLocationStyle;
    private AMapLocation amapLocation;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private Circle circle;
    private NumberPickerView numberPickerView;
    private MarkerOptions markerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    @AfterViews
    void afterViews() {
        map_view.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = map_view.getMap();
            setUpMap();
        }
        init();
    }


    private void init() {
        if (PubFunction.isConnect(activity)) {
            HttpGetFence();
        }
    }


    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {


        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.location_marker);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int newWidth = PubFunction.dip2px(activity, 29);
        int newHeight = PubFunction.dip2px(activity, 29);
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        BitmapDescriptor var = fromBitmap(newbm);

        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(var);
        myLocationStyle.strokeColor(0x00000000);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(0.0f);// 设置圆形的边框粗细

        aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE));
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种


        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.zoomTo(9));
        aMap.getUiSettings().setTiltGesturesEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        // 设置收缩按钮
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);

        // 绘制一个圆形
        circle = aMap.addCircle(new CircleOptions().center(Constants.BEIJING).radius(4000).strokeColor(Color.argb(50, 1, 1, 1)).fillColor(Color.argb(50, 1, 1, 1)).strokeWidth(5));
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        map_view.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        map_view.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map_view.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        map_view.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        this.amapLocation = amapLocation;
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void select_panel() {
        final BottomView bottomView = new BottomView(activity, R.style.BottomViewTheme_Defalut, R.layout.main_car_panel_bottom_view);
        bottomView.setAnimation(R.style.popwin_anim_style);
        View view = bottomView.getView();

        numberPickerView = (NumberPickerView) view.findViewById(R.id.picker);
        numberPickerView.setDisplayedValues(dataString);
        numberPickerView.setMinValue(0);
        numberPickerView.setMaxValue(dataString.length - 1);

        TextView submit = (TextView) view.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PubFunction.isConnect(activity)) {

                    select_text.setText(numberPickerView.getContentByCurrValue());
                    int a = numberPickerView.getPickedIndexRelativeToRaw();
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(13 - a));
                    circle.setRadius(dataInt[a]);
                    LatLng start = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());

                    circle.setCenter(start);
                    HttpSetFence(amapLocation.getLongitude() + "", amapLocation.getLatitude() + "", dataInt[a] + "");
                    bottomView.dismissBottomView();

                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("jingdu", amapLocation.getLongitude() + "");
                    bundle.putString("weidu", amapLocation.getLatitude() + "");
                    bundle.putString("radius", dataInt[a] + "");
                    message.setData(bundle);
                }

            }
        });
        bottomView.showBottomView(true);
    }


    /**
     * http接口：Device/fence     获取上次的围栏  信息
     */
    @UiThread
    void HttpGetFenceSuccess(String messageString, String data) {
        progressDialog.dismiss();

        if (data.equals("")) {
            MyToast.showTheToast(activity, "无数据返回！");
        } else {

            try {
                JSONTokener jsonTokener = new JSONTokener(data);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                String id = jsonObject.getString("id");
                String jingdu = jsonObject.getString("jingdu");
                String weidu = jsonObject.getString("weidu");
                String radius = jsonObject.getString("radius");

                int radius_int = Integer.parseInt(radius);
                int radius_int_small = radius_int / 1000;
                double jingdu_doublr = Double.parseDouble(jingdu);
                double weidu_doublr = Double.parseDouble(weidu);
                circle.setRadius(radius_int);
                LatLng start = new LatLng(weidu_doublr, jingdu_doublr);
                circle.setCenter(start);
                select_text.setText(radius_int_small + "km");


                Resources res_0 = getResources();
                Bitmap bmp_0 = BitmapFactory.decodeResource(res_0, R.drawable.map_c_local);
                int width = bmp_0.getWidth();
                int height = bmp_0.getHeight();
                // 设置想要的大小
                int newWidth = PubFunction.dip2px(activity, 10);
                int newHeight = PubFunction.dip2px(activity, 17);
                // 计算缩放比例
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                // 取得想要缩放的matrix参数
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                Bitmap newbm_0 = Bitmap.createBitmap(bmp_0, 0, 0, width, height, matrix, true);
                bmp_0.recycle();
                bmp_0 = null;
                BitmapDescriptor bitmapDescriptor_0 = fromBitmap(newbm_0);

                LatLng marker = new LatLng(weidu_doublr, jingdu_doublr);
                markerOptions = new MarkerOptions().icon(bitmapDescriptor_0).anchor(0.5f, 0.5f).position(marker).draggable(true);
                aMap.addMarker(markerOptions);

                main_panel.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @UiThread
    void HttpGetFenceError(String messageString) {
        MyToast.showTheToast(activity, messageString);
        progressDialog.dismiss();
    }

    @Background
    void HttpGetFence() {
        String path = PubFunction.app + "Device/fence.html";
        HttpPost httpPost = new HttpPost(path);
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

        PubFunction.setHandlerWhit_APTK_APUD(activity, "app", uid, httpPost);

        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("uid", uid));


        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String code = jsonObject.getString("status");
                String messageString = jsonObject.getString("msg");

                if (code.equals("1")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        HttpGetFenceSuccess(messageString, data);
                    } else {
                        HttpGetFenceSuccess(messageString, "");
                    }
                } else {
                    HttpGetFenceError(messageString);
                }
            } else {
                HttpGetFenceError("服务器错误：HttpGetFence");
            }
        } catch (Exception e) {
            HttpGetFenceError("json解析错误：HttpGetFence");
        }
    }


    /**
     * http接口：Device/setFence     设置电子围栏信息
     */
    @UiThread
    void HttpSetFenceSuccess(String messageString, String data) {
        progressDialog.dismiss();
        MyToast.showTheToast(activity, messageString);
//        aMap.clear();
        HttpGetFence();

    }

    @UiThread
    void HttpSetFenceError(String messageString) {
        MyToast.showTheToast(activity, messageString);
        progressDialog.dismiss();
    }

    @Background
    void HttpSetFence(String jingdu, String weidu, String radius) {
        String path = PubFunction.app + "Device/setFence.html";
        HttpPost httpPost = new HttpPost(path);
        httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

        PubFunction.setHandlerWhit_APTK_APUD(activity, "app", uid, httpPost);

        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("uid", uid));
        list.add(new BasicNameValuePair("jingdu", jingdu));
        list.add(new BasicNameValuePair("weidu", weidu));
        list.add(new BasicNameValuePair("radius", radius));
        try {
            HttpEntity entity = new UrlEncodedFormEntity(list, "utf-8");
            httpPost.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

                System.out.println(jsonObject);

                String code = jsonObject.getString("status");
                String messageString = jsonObject.getString("msg");

                if (code.equals("1")) {
                    if (jsonObject.has("data")) {
                        String data = jsonObject.getString("data");
                        HttpSetFenceSuccess(messageString, data);
                    } else {
                        HttpSetFenceSuccess(messageString, "");
                    }
                } else {
                    HttpSetFenceError(messageString);
                }
            } else {
                HttpSetFenceError("服务器错误：HttpSetFence");
            }
        } catch (Exception e) {
            HttpSetFenceError("json解析错误：HttpSetFence");
        }
    }


}
