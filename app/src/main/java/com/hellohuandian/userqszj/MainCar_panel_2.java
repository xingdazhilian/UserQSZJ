package com.hellohuandian.userqszj;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.gaode.AMapUtil;
import com.hellohuandian.userqszj.pub.gaode.DrivingRouteOverlay;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import static com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap;
import static java.lang.Thread.sleep;

@EActivity(R.layout.main_car_panel_2)
public class MainCar_panel_2 extends BaseActivity implements LocationSource, AMapLocationListener, AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, RouteSearch.OnRouteSearchListener {

    private final int ROUTE_TYPE_DRIVE = 2;
    @ViewById
    LinearLayout page_return;
    @ViewById
    MapView map_view;
    @ViewById
    TextView find_car;
    @ViewById
    RelativeLayout main_panel;
    private Bundle savedInstanceState;
    private AMap aMap;
    private UiSettings mUiSettings;
    private GeocodeSearch geocoderSearch;
    private MyLocationStyle myLocationStyle;
    private AMapLocation amapLocation;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，39.942295,116.335891
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，39.995576,116.481288
    private RelativeLayout mBottomLayout, mHeadLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ProgressDialog progDialog = null;// 搜索时进度条

    private int is_local = 0;
    private int is_local_code = 0;

    private String car_id = "";


    @Background
    void setLocal() {
        while (is_local_code == 0) {
            try {

                sleep(10);
                if (is_local == 1) {
                    setUpMap();
                    is_local_code = 1;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    @AfterViews
    void afterViews() {

        car_id = getIntent().getStringExtra("car_id");

        map_view.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = map_view.getMap();
            setUpMap();
        }

        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        if (PubFunction.isConnect(activity)) {
            HttpGetLocal();
        }
    }


    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions().position(AMapUtil.convertToLatLng(mEndPoint)).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_c_local)));
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setInfoWindowAdapter(this);

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
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                is_local = 1;
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


    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            MyToast.showTheToast(activity, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            MyToast.showTheToast(activity, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null, null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths().get(0);
                    if (drivePath == null) {
                        return;
                    }
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(activity, aMap, drivePath, mDriveRouteResult.getStartPos(), mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    mRouteDetailDes.setText("打车约" + taxiCost + "元");
                    mBottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, MainCar_panel_2.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result", mDriveRouteResult);
                            startActivity(intent);
                        }
                    });

                } else if (result != null && result.getPaths() == null) {
                    MyToast.showTheToast(activity, "失败");
                }

            } else {
                MyToast.showTheToast(activity, "失败");
            }
        } else {
            MyToast.showTheToast(activity, errorCode + "");
        }


    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }


    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void find_car() {
        if (PubFunction.isConnect(activity)) {
            mStartPoint = new LatLonPoint(amapLocation.getLatitude(), amapLocation.getLongitude());
            searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
        }
    }


    /**
     * http接口:获取定位信息
     */
    @Background
    void HttpGetLocal() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("url", "carAction!getPositionByID.do"));
        dataList.add(new ParamTypeData("gps", car_id));
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("type", "3"));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mapType", "2");
            dataList.add(new ParamTypeData("param", jsonObject.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new OkHttpConnect(activity, PubFunction.app + "rpc/send.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetLocal(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetLocal(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                System.out.println(jsonObject_response);
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                        if (jsonObject.has("res")) {
                            String msg = jsonObject.getString("res");
                            if (msg.equals("true")) {
                                String data = jsonObject.getString("result");
                                main_panel.setVisibility(View.VISIBLE);
                                try {
                                    JSONTokener jsonTokener = new JSONTokener(data);
                                    JSONArray jsonArray = (JSONArray) jsonTokener.nextValue();
                                    JSONObject jsonObject_small = jsonArray.getJSONObject(0);
                                    String lat = jsonObject_small.getString("lat");
                                    String lng = jsonObject_small.getString("lng");
                                    double lat_D = Double.parseDouble(lat);
                                    double lng_D = Double.parseDouble(lng);
                                    mEndPoint = new LatLonPoint(lat_D, lng_D);
                                    setfromandtoMarker();
                                } catch (Exception e) {
                                    MyToast.showTheToast(activity, "泰比特数据解析错误");
                                }
                            } else {
                                MyToast.showTheToast(activity, "泰比特返回数据错误");
                            }
                        } else {
                            MyToast.showTheToast(activity, "返回数据错误：HttpGetData");
                        }
                    } else {
                        MyToast.showTheToast(activity, "没有返回值");
                    }
                } else {
                    String msg = jsonObject_response.getString("msg");
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

}
