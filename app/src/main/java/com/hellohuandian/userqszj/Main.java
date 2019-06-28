package com.hellohuandian.userqszj;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CustomMapStyleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.hellohuandian.userqszj.bean.MarkerBean;
import com.hellohuandian.userqszj.bean.MarkerInfoBean;
import com.hellohuandian.userqszj.fragment.NoLocalFragment;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.CreateFile;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.gaode.RideRouteCalculateActivity;
import com.hellohuandian.userqszj.util.Util;
import com.hellohuandian.userqszj.view.MyScrollView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

@SuppressLint("Registered")
@EActivity(R.layout.main)
public class Main extends BaseActivity implements
        AMap.OnMarkerClickListener,//marker点击事件监听接口
        AMap.OnMapClickListener,//地图点击事件监听接口
        AMap.CancelableCallback,
        AMap.OnMapLongClickListener {//地图长按事件监听接口
    //定位范围圆形内的填充颜色
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final int PERMISSON_REQUESTCODE = 0;
    private static final int REQUEST_PERMISSION_SETTING = 1;
    //用户正常登陆、注销后激活登陆刷新地图marker
    public static Handler handleRefreshMarker;
    //等待地图marker图片全部下载完成后再通知显式
    public static Handler handleImageLoaded;
    //等待所创建的Bitmap图形加载完毕再进行测量绘制等
    public static Handler handleImageDrawn;
    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
    };
    @ViewById
    LinearLayout user_info, set_location, phone, scan_code, ll_point_container, nav, nor_read_black_panel, list_panel, page_close,
            site0_linearLayout, site1_linearLayout, site2_linearLayout, site3_linearLayout, site4_linearLayout;
    @ViewById
    FrameLayout shop, nor_read_red_point_panel;
    @ViewById
    DrawerLayout drawer_layout;
    @ViewById
    ListView left_drawer;
    @ViewById
    MapView mMapView;
    @ViewById
    MyScrollView myScrollView;
    @ViewById
    ViewPager viewPager;
    @ViewById
    TextView nor_read_red_point, panel_title, foot_text, site0_textView, site1_textView, site2_textView, site3_textView, site4_textView;
    @ViewById
    ImageView site0_imageView, site1_imageView, site2_imageView, site3_imageView, site4_imageView;
    MyLocationStyle myLocationStyle;
    private NoLocalFragment noLocalFragment = NoLocalFragment.getInstance();
    private int img_count;
    private Bitmap[] bitmaps;
    //判断是否可以成功定位，否则弹出提示
    private boolean positioned = false;
    private Bundle savedInstanceState;
    private LatLng location1;
    private List<Map<String, Object>> dataList = new ArrayList<>();
    private String[] arrarStr = {"我的钱包", "我的订单", "我的优惠券", "骑士之家客服", "我的电动车", "电池租用", "充电器设置", "系统设置"};
    private int[] arrarinco = {R.drawable.b_04, R.drawable.b_10, R.drawable.b_09, R.drawable.b_03, R.drawable.b_15, R.drawable.b_16, R.drawable.b_13, R.drawable.b_05};
    private ImageView user_img;
    private TextView user_phone;
    private int left_info_state = 0;
    //记录服务器返回的空坐标的数量，用于判断最后地图marker是否全部构建好了
    private int errorPosition = 0;
    //地图
    private AMap aMap;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private List<Marker> markerList = new ArrayList<>();
    //是否正在请求marker标记，请求过程中为了避免重复点击按钮
    private boolean isRequestMarkerNow = false;
    private List<MarkerOptions> markerOptionsList = new ArrayList<>();
    private List<MarkerBean.DataBean> dataBeanList = new ArrayList<>();
    //点击坐标的弹窗
    private int viewpager_count = 0;
    private List<View> viewList = new ArrayList<>();// 将要分页显示的View装入数组中
    private ImageView[] imageViews = new ImageView[viewpager_count];
    private boolean isFirstIn = true;
    private boolean isExit = false;
    //判断是否需要检测，防止不停的弹框
    private boolean isNeedCheck = true;
    //保存定位得到的经度和纬度，传给后台得到该位置的marker
    private Double longitude = 0.0;
    private Double latitude = 0.0;
    private MarkerBean bean;
    //请求站点的类型：0全部，1综合站，2维修站，3便利店，-1换电站
    private int type = 0;
    //定位成功监听
    private AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
//                StringBuilder sb = new StringBuilder();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    Log.d("051101", "定位成功1");
                    if (!positioned) {
                        positioned = true;
                        //定位成功，记录经纬度
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        //定位蓝点恢复显示
                        if (myLocationStyle != null) {
                            aMap.setMyLocationStyle(myLocationStyle);
                        } else {
                            setupLocationStyle();
                        }
                    }
                    //定位成功，移动视图
                    location1 = new LatLng(location.getLatitude(), location.getLongitude());
                    aMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(new CameraPosition(location1, 18, 0, 0)),
                            888,
                            Main.this);
                } else {
                    //定位失败
                    positioned = false;
                    Log.d("051101", "定位失败2");
//                    //定位失败
//                    sb.append("定位失败" + "\n");
//                    sb.append("错误码:").append(location.getErrorCode()).append("\n");
//                    sb.append("错误信息:").append(location.getErrorInfo()).append("\n");
//                    sb.append("错误描述:").append(location.getLocationDetail()).append("\n");
//                    //解析定位结果，
//                    String result = sb.toString();
//                    Toast.makeText(Main.this, result, Toast.LENGTH_SHORT).show();
                }
            } else {
                positioned = false;
                Log.d("051101", "定位失败3");
            }
            if (!positioned) {
                //如果已获得所需的权限，才显示开启GPS提示框，否则申请权限框和打开GPS框会重叠
                if (findDeniedPermissions(needPermissions).size() <= 0) {
                    show_no_local();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private void handler() {
        handleRefreshMarker = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                type = 0;
                resetSitesBackground();
                uid = sharedPreferences.getString("id", "");
                startDrawMarker();
            }
        };
        handleImageLoaded = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                img_count++;
                int img_position = msg.arg1;
                bitmaps[img_position] = (Bitmap) msg.obj;
//                Log.i("052001", "下载了" + img_count + "张图片");
                if (img_count == bitmaps.length) {
//                    Log.i("052001", "全部下载完了");
                    dataBeanList = bean.getData();
                    for (int i = 0; i < dataBeanList.size(); i++) {
                        MarkerBean.DataBean dataBean = dataBeanList.get(i);
                        if (!dataBean.getJingdu().equals("") && !dataBean.getWeidu().equals("")) {
                            if (dataBean.getOpen_door() == 0) {
                                getBitmap(2, 0, Double.valueOf(dataBean.getWeidu()), Double.valueOf(dataBean.getJingdu()), dataBean.getId(), dataBean.getBtype());
                            } else {
                                getBitmap(dataBean.getImg_idx(), dataBean.getUsable(), Double.valueOf(dataBean.getWeidu()), Double.valueOf(dataBean.getJingdu()), dataBean.getId(), dataBean.getBtype());
                            }
                        } else {
                            errorPosition++;
                        }
                    }
                }
            }
        };
        handleImageDrawn = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                View view = (View) msg.obj;
                Bundle bundle = msg.getData();
                Double weidu = bundle.getDouble("weidu");
                Double jingdu = bundle.getDouble("jingdu");
                String id = bundle.getString("id");
                int width = view.getWidth();
                int height = view.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(0, 0, width, height);
                view.draw(canvas);

                int width_1 = bitmap.getWidth();
                int height_1 = bitmap.getHeight();
//                Log.i("051401", "width:" + width_1 + ",height:" + height_1);
                //设置需要的宽高大小
                int newWidht_1 = PubFunction.dip2px(Main.this, 37);
                int newHeight_1 = PubFunction.dip2px(Main.this, 97);
                //计算缩放比例
                float scaleWidth_1 = (float) newWidht_1 / width_1;
                float scaleHeight_1 = (float) newHeight_1 / height_1;
                //取得想要缩放的matrix参数
                Matrix matrix_1 = new Matrix();
                matrix_1.postScale(scaleWidth_1, scaleHeight_1);
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width_1, height_1, matrix_1, true);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(newBitmap);
                LatLng latLng = new LatLng(weidu, jingdu);
                MarkerOptions markerOption = new MarkerOptions()
                        .title(id)
                        .icon(bitmapDescriptor)
                        .anchor(0.5f, 0.5f)
                        .position(latLng)
                        .draggable(false);
                markerOptionsList.add(markerOption);
                newBitmap.recycle();
                //集中显示
                if (markerOptionsList.size() == dataBeanList.size() - errorPosition) {
                    for (MarkerOptions markerOption1 : markerOptionsList) {
                        Marker marker = aMap.addMarker(markerOption1);
                        marker.setInfoWindowEnable(false);
                        markerList.add(marker);
                    }
                }

            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果这里获取mMapView会获取为null，所以保存savedInstanceState，
        //然后在afterViews()执行mMapView.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState;
    }

    @AfterViews
    void afterViews() {
        handler();
        //生成文件夹
        new CreateFile(activity);
        //初始化View
        initView();
        //map相关初始化
        InitMap();
        //版本更新接口
        HttpGetVer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //等到有坐标了 开始定位
        //在activity执行onResume时执行mMapView.onResume()，重新绘制加载地图
        mMapView.onResume();
        //申请权限
        if (Build.VERSION.SDK_INT >= 23 && isNeedCheck) {
            checkPermissions(needPermissions);
        }
        if (!positioned) {
            startLocation();//开始定位
        }
        sharedPreferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString("id", "");
        if (isFirstIn && !isRequestMarkerNow) {
            startDrawMarker();//获取地图marker
        }
        //获取消息信息
        //获取用户信息
        if (!uid.equals("")) {
            progressDialog.show();
            HttpGetUserInfo();
            HttpGetMainInfo();
        } else {
            nor_read_red_point_panel.setVisibility(View.GONE);
        }
        if (PubFunction.isConnect(activity, false)) {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
        System.gc();
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        if (mLocationClient == null) {
            initLocation();
        }
        mLocationClient.startLocation();
    }

    /**
     * 获取地图marker
     */
    @Background
    void startDrawMarker() {
        isRequestMarkerNow = true;
        //限制倒计时时长5秒
        //如果5秒内获取到定位坐标，优先传坐标获取定位marker
        //5秒内定位失败获取不到坐标，传uid获取定位marker
        int until = 0;
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            until++;
            if (positioned || until >= 5) {
                //获取地图marker
                HttpGetAllMarker();
                break;
            }
        }
    }

    private void InitMap() {
        //弹出动画处理
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(myScrollView, "translationY", 0, PubFunction.dip2px(this, 240));
        objectAnimator.setDuration(0);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                myScrollView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        objectAnimator.start();
        mMapView.onCreate(savedInstanceState);//此方法必须重写
        if (aMap == null) {
            aMap = mMapView.getMap();
            //自定义地图样式   https://lbs.amap.com/api/android-sdk/guide/create-map/custom
            CustomMapStyleOptions customMapStyleOptions = getCustomMapStyleOptions(
                    getApplicationContext(), "style.data");
            if (customMapStyleOptions != null) {
                customMapStyleOptions.setEnable(true);
                aMap.setCustomMapStyle(customMapStyleOptions);
            }
            //自定义定位蓝点样式
            setupLocationStyle();
            UiSettings mUiSettings = aMap.getUiSettings();
            //设置地图默认的缩放按钮是否显示
            mUiSettings.setZoomControlsEnabled(false);
            //设置地图是否可以旋转
            mUiSettings.setRotateGesturesEnabled(false);
            //设置地图是否可以倾斜
            mUiSettings.setTiltGesturesEnabled(false);
            //设置logo下移隐藏
            mUiSettings.setLogoBottomMargin(-100);
            //是否可触发定位并显示定位层（定位蓝点）
            aMap.setMyLocationEnabled(true);
            aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
            //初始化定位
            initLocation();
            //设置监听事件
            aMap.setOnMarkerClickListener(this);//设置点击marker事件监听器
            aMap.setOnMapClickListener(this);//设置map单击事件监听器
            aMap.setOnMapLongClickListener(this);//设置map长按事件监听器
        }
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationOption = new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置单次定位
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //设置定位监听
        mLocationClient.setLocationListener(locationListener);
    }

    /**
     * 设置自定义定位蓝点样式
     */
    private void setupLocationStyle() {
        //定位图标处理
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_marker);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float newWidthSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, metrics);
        float newHeightSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
        //计算缩放比例
        float scaleWidth = newWidthSize / width;
        float scaleHeight = newHeightSize / height;
        //取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        //自定义系统定位蓝点
        myLocationStyle = new MyLocationStyle();
        //自定义定位蓝点图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(newBitmap);
        myLocationStyle.myLocationIcon(bitmapDescriptor);
        //自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(0x00000000);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0.0f);
        //设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        //定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 获取assets目录下的高德自定义地图style.data
     *
     * @param context  Context
     * @param fileName assets目录下的文件名
     * @return CustomMapStyleOptions
     */
    public CustomMapStyleOptions getCustomMapStyleOptions(Context context, String fileName) {
        InputStream inputStream = null;
        AssetManager assetManager = context.getAssets();
        try {
            inputStream = assetManager.open(fileName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            CustomMapStyleOptions mapStyleOptions = new CustomMapStyleOptions();
            mapStyleOptions.setStyleData(b);
            return mapStyleOptions;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //控件相关
    @SuppressLint("SetTextI18n")
    private void initView() {
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (PubFunction.isConnect(activity)) {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                } else {
                    drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                left_info_state = 1;
                Log.d("wyy", "侧拉菜单打开了");
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                left_info_state = 0;
                Log.d("wyy", "侧拉菜单关闭了");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        //左面抽屉和足电池弹窗相关
        View vHead = View.inflate(this, R.layout.main_left_top, null);
        vHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, MainInfo_.class));
            }
        });
        foot_text.setText("V " + PubFunction.getLocalVersionName(activity));
        user_img = vHead.findViewById(R.id.user_img);
        user_phone = vHead.findViewById(R.id.user_phone);
        left_drawer.addHeaderView(vHead);
        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), getdata(), R.layout.main_left_item, new String[]{"text", "icon"}, new int[]{R.id.text1, R.id.icon});
        left_drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    activity.startActivity(new Intent(activity, MainWallet_.class));
                } else if (position == 2) {
                    activity.startActivity(new Intent(activity, MainMessage_1_.class));
                } else if (position == 3) {
                    activity.startActivity(new Intent(activity, MainDiscount_.class));
                } else if (position == 4) {
                    activity.startActivity(new Intent(activity, MainCustomer_.class));
                } else if (position == 5) {
                    activity.startActivity(new Intent(activity, MainCarQSZJ_.class));
                } else if (position == 6) {
                    activity.startActivity(new Intent(activity, MainBar_.class));
                } else if (position == 7) {
                    activity.startActivity(new Intent(activity, MainBlueTooth_.class));
                } else if (position == 8) {
                    activity.startActivity(new Intent(activity, MainSet_.class));
                }
                drawer_layout.closeDrawer(list_panel);
            }
        });
        left_drawer.setAdapter(simpleAdapter);
    }

    @Click({R.id.user_info, R.id.shop, R.id.set_location, R.id.phone, R.id.scan_code, R.id.page_close,
            R.id.site0_linearLayout, R.id.site1_linearLayout, R.id.site2_linearLayout, R.id.site3_linearLayout, R.id.site4_linearLayout})
    void click(View bt) {
        switch (bt.getId()) {
            case R.id.user_info:
                if (PubFunction.isConnect(activity)) {
                    if (uid.equals("")) {
                        activity.startActivity(new Intent(activity, Login_.class));
                    } else {
                        HttpGetUserInfo();
                        drawer_layout.openDrawer(list_panel);
                    }
                }
                break;
            case R.id.shop:
                if (PubFunction.isConnect(activity)) {
                    if (uid.equals("")) {
                        activity.startActivity(new Intent(activity, Login_.class));
                    } else {
                        activity.startActivity(new Intent(activity, MainMessage_4_.class));
                    }
                }
                break;
            case R.id.set_location:
                positioned = false;
                startLocation();
                break;
            case R.id.phone:
                if (PubFunction.isConnect(activity)) {
                    if (uid.equals("")) {
                        activity.startActivity(new Intent(activity, Login_.class));
                    } else {
                        activity.startActivity(new Intent(activity, MainCustomer_.class));
                    }
                }
                break;
            case R.id.scan_code:
                if (PubFunction.isConnect(activity)) {
                    if (uid.equals("")) {
                        MyToast.showTheToast(activity, "请先进行登录！");
                        activity.startActivity(new Intent(activity, Login_.class));
                    } else {
                        Intent intent = new Intent(activity, MainShop_.class);
                        activity.startActivity(intent);
                    }
                }
                break;
            case R.id.page_close:
                myScrollView.A_1_close();
                break;
            case R.id.site0_linearLayout:
                if (isRequestMarkerNow) {
                    MyToast.showTheToast(this, "正在请求站点数据，请耐心等候");
                } else {
                    resetSitesBackground();
                    type = 0;
                    MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
                    startDrawMarker();
                }
                break;
            case R.id.site1_linearLayout:
                if (isRequestMarkerNow) {
                    MyToast.showTheToast(this, "正在请求站点数据，请耐心等候");
                } else {
                    resetSitesBackground();
                    site1_linearLayout.setBackgroundResource(R.drawable.site1_checked);
                    site1_imageView.setBackgroundResource(R.drawable.site1_white);
                    site1_textView.setTextColor(getResources().getColor(android.R.color.white));
                    type = -1;
                    MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
                    startDrawMarker();
                }
                break;
            case R.id.site2_linearLayout:
                if (isRequestMarkerNow) {
                    MyToast.showTheToast(this, "正在请求站点数据，请耐心等候");
                } else {
                    resetSitesBackground();
                    site2_linearLayout.setBackgroundColor(Color.parseColor("#79512a"));
                    site2_imageView.setBackgroundResource(R.drawable.site2_white);
                    site2_textView.setTextColor(getResources().getColor(android.R.color.white));
                    type = 2;
                    MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
                    startDrawMarker();
                }
                break;
            case R.id.site3_linearLayout:
                if (isRequestMarkerNow) {
                    MyToast.showTheToast(this, "正在请求站点数据，请耐心等候");
                } else {
                    resetSitesBackground();
                    site3_linearLayout.setBackgroundColor(Color.parseColor("#ff9320"));
                    site3_imageView.setBackgroundResource(R.drawable.site3_white);
                    site3_textView.setTextColor(getResources().getColor(android.R.color.white));
                    type = 1;
                    MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
                    startDrawMarker();
                }
                break;
            case R.id.site4_linearLayout:
                if (isRequestMarkerNow) {
                    MyToast.showTheToast(this, "正在请求站点数据，请耐心等候");
                } else {
                    resetSitesBackground();
                    site4_linearLayout.setBackgroundResource(R.drawable.site4_checked);
                    site4_imageView.setBackgroundResource(R.drawable.site4_white);
                    site4_textView.setTextColor(getResources().getColor(android.R.color.white));
                    type = 3;
                    MyToast.showTheToast(this, "正在刷新站点数据，请稍候");
                    startDrawMarker();
                }
                break;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (left_info_state == 1) {
                drawer_layout.closeDrawers();
            } else {
                exitByDoubleClick();
            }
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            MyToast.showTheToast(activity, "再按一次退出程序!");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;//取消退出
                }
            }, 2000);// 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        destroyLocation();
        aMap.clear();
        aMap = null;
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (mLocationClient != null) {
            /*
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
            mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
            mLocationClient = null;
            mLocationOption = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    //左边list的数据
    private List<Map<String, Object>> getdata() {
        for (int i = 0; i < arrarStr.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("text", arrarStr[i]);
            map.put("icon", arrarinco[i]);
            dataList.add(map);
        }
        return dataList;
    }

    private void show_no_local() {
        if (!noLocalFragment.isVisible()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            noLocalFragment.setOnDialogItemClickListener(new NoLocalFragment.OnDialogItemClickListener() {
                @Override
                public void onSuccessClick() {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }

                @Override
                public void onErrorClick() {
                }
            });
            noLocalFragment.show(fragmentTransaction, "NOLOCAL");
        }
    }

    /**
     * http接口：App/version.html   获取当前版本号
     */
    @Background
    void HttpGetVer() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.api + "AppVer/version.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetVer(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetVer(String response, String type) {
//        Util.d("052101", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");
//                    String Android_name = jsonObject.getString("Android_name");
                    String Android_code = jsonObject.getString("Android_code");
                    final String Android_url = jsonObject.getString("Android_url");
                    String Android_force = jsonObject.getString("Android_force");
                    int Android_code_int = Integer.parseInt(Android_code);
                    if (Android_force.equals("0")) {
                        PackageManager manager = activity.getPackageManager();
                        PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
                        final int versionCodeLocal = info.versionCode;
                        if (Android_code_int > versionCodeLocal) {
                            LayoutInflater inflater = LayoutInflater.from(activity);
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            final AlertDialog mAlertDialog = builder.create();
                            View view = inflater.inflate(R.layout.alertdialog_update, null);
                            TextView success = view.findViewById(R.id.payAlertdialogSuccess);
                            success.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    new DownLoadAndInstallApp(activity, Android_url, "myApp.apk");
                                    MyToast.showTheToast(activity, "正在后台进行下载，请稍后！");
                                    mAlertDialog.dismiss();
                                }
                            });
                            TextView error = view.findViewById(R.id.payAlertdialogError);
                            error.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    mAlertDialog.dismiss();
                                }
                            });
                            mAlertDialog.setCancelable(false);
                            mAlertDialog.show();
                            mAlertDialog.getWindow().setContentView(view);
                        }
                    } else {
                        PackageManager manager = activity.getPackageManager();
                        PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
                        final int versionCodeLocal = info.versionCode;
                        if (Android_code_int > versionCodeLocal) {
                            new DownLoadAndInstallApp(activity, Android_url, "myApp.apk");
                            MyToast.showTheToast(activity, "正在更新APP，请稍后！");
                        }
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON3：" + e.toString());
            }
        }
    }

    /**
     * http接口：User/personal.html    获取用户信息
     */
    @Background
    void HttpGetUserInfo() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "User/personal.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetUserInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetUserInfo(String response, String type) {
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
                    final String img = jsonObject.getString("avater");
                    String phone = jsonObject.getString("phone");
//                    String str1 = phone.substring(0, 3);
//                    String str3 = phone.substring(7, 11);
                    Picasso.with(activity).load(img).into(user_img);
                    user_phone.setText(phone);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_img_url", img);
                    editor.apply();
                } else {
                    MyToast.showTheToast(activity, msg);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON1：" + e.toString());
            }
        }
    }

    /**
     * http接口：Map/mapJwdu.html    获取站点坐标信息
     */
    @Background
    void HttpGetAllMarker() {
//        Log.i("051502", "获取地图标记了");
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("jingdu", longitude == 0 ? "" : longitude.toString()));
        dataList.add(new ParamTypeData("weidu", latitude == 0 ? "" : latitude.toString()));
        dataList.add(new ParamTypeData("type", String.valueOf(type)));
        new OkHttpConnect(this, PubFunction.app + "Map/mapJwduV2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD(this, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetAllMarker(response, type);
            }
        }).startHttpThread();
    }

    @SuppressLint("CheckResult")
    @UiThread
    void onDataHttpGetAllMarker(String response, String type) {
//        Util.d("050901", response);
        if (type.equals("0")) {
            System.out.println(response);
        } else {
            clearMarkers();
            //定位蓝点恢复显示
            if (myLocationStyle != null) {
                aMap.setMyLocationStyle(myLocationStyle);
            } else {
                setupLocationStyle();
            }
            Gson gson = new Gson();
            bean = gson.fromJson(response, MarkerBean.class);
            int status = bean.getStatus();
            String msg = bean.getMsg();
            if (status == 1) {
                isFirstIn = false;
                List<String> img_list = bean.getImg_list();
                bitmaps = new Bitmap[img_list.size()];
                for (int i = 0; i < img_list.size(); i++) {
                    final int finalI = i;
                    Glide.with(this)
                            .asBitmap()
                            .load(img_list.get(finalI))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    Message message = new Message();
                                    message.arg1 = finalI;
                                    message.obj = resource;
                                    handleImageLoaded.sendMessage(message);
                                }
                            });
                }
            } else {
                MyToast.showTheToast(this, msg);
            }
        }
        isRequestMarkerNow = false;
    }

    void getBitmap(int img_idx, int usable, final Double weidu, final Double jingdu, final String id, int btype) {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;// 屏幕宽度（像素）
        int height = metric.heightPixels;// 屏幕高度（像素）
        final View view = LayoutInflater.from(this).inflate(R.layout.main_map_marker_icon, null, false);
        TextView t_1 = view.findViewById(R.id.t_1);
        t_1.setText(String.valueOf(usable));
        if (img_idx == 0 || img_idx == 3 || img_idx == 7 || img_idx == 8) {
            t_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        } else if (img_idx == 2 || img_idx == 5 || img_idx == 6) {
            t_1.setText("");
        }
        if (btype == 3) {
            t_1.setTextColor(Color.parseColor("#4f558f"));
        } else {
            t_1.setTextColor(Color.parseColor("#fb2d08"));
        }
        view.layout(0, 0, width, height);
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        final ImageView i_1 = view.findViewById(R.id.i_1);
//        String uri = img_list.get(img_idx);
        Glide.with(getApplicationContext())
                .load(bitmaps[img_idx])
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        i_1.setImageDrawable(resource);
                        Bundle bundle = new Bundle();
                        bundle.putDouble("weidu", weidu);
                        bundle.putDouble("jingdu", jingdu);
                        bundle.putString("id", id);
                        Message message = new Message();
                        message.obj = view;
                        message.setData(bundle);
                        handleImageDrawn.sendMessage(message);
                    }
                });
    }

    /**
     * 清除
     */
    private void clearMarkers() {
        img_count = 0;
        errorPosition = 0;
        List<Marker> mapScreenMarkers = markerList;
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            marker.remove();//移除当前Marker
        }
//        aMap.clear();
        markerList.clear();
        markerOptionsList.clear();
    }

    /**
     * http接口：Advices/nums.html    获取屏幕信息
     */
    @Background
    void HttpGetMainInfo() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "Advices/nums.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetMainInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpGetMainInfo(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            nor_read_red_point_panel.setVisibility(View.GONE);
            nor_read_black_panel.setVisibility(View.GONE);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                if (status.equals("1")) {
                    JSONObject jsonObject = jsonObject_response.getJSONObject("data");
                    nor_read_red_point_panel.setVisibility(View.VISIBLE);
                    nor_read_black_panel.setVisibility(View.VISIBLE);
                    String show_num;
                    int advices_num = Integer.parseInt(jsonObject.getString("advices_num"));
                    if (advices_num > 99) {
                        show_num = "99+";
                    } else {
                        show_num = advices_num + "";
                    }
                    if (advices_num > 0) {
                        nor_read_red_point.setText(show_num);
                        Animation mAnimation = AnimationUtils.loadAnimation(this, R.anim.main_top_anim);
                        nor_read_red_point_panel.setAnimation(mAnimation);
                        mAnimation.start();
                    } else {
                        nor_read_red_point_panel.setVisibility(View.GONE);
                    }
                    if (jsonObject.has("lists")) {
                        JSONObject jsonObject_1 = jsonObject.getJSONObject("lists");
                        String title = jsonObject_1.getString("title");
                        panel_title.setText(title);
                        final String url = jsonObject_1.getString("url");
                        final String id_url = jsonObject_1.getString("id");
                        if (!url.equals("")) {
                            nor_read_black_panel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(activity, MainAdv_.class);
                                    intent.putExtra("url", url);
                                    activity.startActivity(intent);
                                }
                            });
                        } else {
                            nor_read_black_panel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(activity, MainMessage_4_Content_ID_.class);
                                    intent.putExtra("id", id_url);
                                    activity.startActivity(intent);
                                }
                            });
                        }
                    } else {
                        nor_read_black_panel.setVisibility(View.GONE);
                    }
                } else {
                    MyToast.showTheToast(activity, msg);
                    nor_read_red_point_panel.setVisibility(View.GONE);
                    nor_read_black_panel.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON2：" + e.toString());
                nor_read_red_point_panel.setVisibility(View.GONE);
                nor_read_black_panel.setVisibility(View.GONE);
            }
        }
    }

    /**
     * http接口：Evaluate/mEvaluate.html    获取单个站点坐标信息
     */
    @Background
    void HttpMarkerInfo(String tid) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("tid", tid));
        new OkHttpConnect(this, PubFunction.app + "Evaluate/mEvaluate.html", dataList, HeaderTypeData.HEADER_Whit_APTK(this), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpMarkerInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @SuppressLint("SetTextI18n")
    @UiThread
    void onDataHttpMarkerInfo(String response, String type) {
//        Util.d("051501", response);
        if (type.equals("0")) {
            System.out.println(response);
        } else {
            Gson gson = new Gson();
            MarkerInfoBean bean = gson.fromJson(response, MarkerInfoBean.class);
            int status = bean.getStatus();
            String msg = bean.getMsg();
            if (status == 1) {
                TextView shop_type = findViewById(R.id.shop_type);//站点类型
                TextView distanceView = findViewById(R.id.distance);//营业状态
                TextView timeView = findViewById(R.id.time);//距离站点位置
                TextView countView = findViewById(R.id.count);//可用电池数量
                LinearLayout count_panel = findViewById(R.id.count_panel);//可用电池数量layout
                TextView star_text = findViewById(R.id.star_text);//商家星级文字描述
                ImageView star_1 = findViewById(R.id.star_1);//5
                ImageView star_2 = findViewById(R.id.star_2);//个
                ImageView star_3 = findViewById(R.id.star_3);//小
                ImageView star_4 = findViewById(R.id.star_4);//星
                ImageView star_5 = findViewById(R.id.star_5);//星
                TextView address = findViewById(R.id.address);//站点地址
                TextView name = findViewById(R.id.name);//站点名
                LinearLayout nav = findViewById(R.id.nav);//导航按钮
                MarkerInfoBean.DataBean dataBean = bean.getData();
                //维修站不显示“可用电池数量”
                if (dataBean.getCab_typer().equals("维修站")) {
                    count_panel.setVisibility(View.GONE);
                } else {
                    count_panel.setVisibility(View.VISIBLE);
                }
                shop_type.setText(dataBean.getCab_typer());
                distanceView.setText(dataBean.getOpen_title());
                countView.setText(String.valueOf(dataBean.getUsable()));
                address.setText("地址：" + dataBean.getAddress());
                name.setText(dataBean.getTitle() + "：" + dataBean.getPhone());
                float evaluate = dataBean.getEvaluate();
                star_1.setImageResource(R.drawable.star_0);
                star_2.setImageResource(R.drawable.star_0);
                star_3.setImageResource(R.drawable.star_0);
                star_4.setImageResource(R.drawable.star_0);
                star_5.setImageResource(R.drawable.star_0);
                if (evaluate >= 1) {
                    star_1.setImageResource(R.drawable.star_1);
                    star_text.setText("1星商家");
                }
                if (evaluate >= 2) {
                    star_2.setImageResource(R.drawable.star_1);
                    star_text.setText("2星商家");
                }
                if (evaluate >= 3) {
                    star_3.setImageResource(R.drawable.star_1);
                    star_text.setText("3星商家");
                }
                if (evaluate >= 4) {
                    star_4.setImageResource(R.drawable.star_1);
                    star_text.setText("4星商家");
                }
                if (evaluate >= 5) {
                    star_5.setImageResource(R.drawable.star_1);
                    star_text.setText("5星商家");
                }
                LatLng start = new LatLng(PubFunction.local[0], PubFunction.local[1]);
                LatLng end = new LatLng(PubFunction.marker[0], PubFunction.marker[1]);
                float distance = AMapUtils.calculateLineDistance(start, end);
                distance = distance * 120 / 100;
                if (distance > 1000) {
                    distance /= 1000;
                    BigDecimal b = new BigDecimal(distance);
                    double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    timeView.setText(f1 + "千米");
                } else {
                    BigDecimal b = new BigDecimal(distance);
                    double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    timeView.setText(f1 + "米");
                }
                myScrollView.A_1();
                if (viewList.size() > 0) {
                    ll_point_container.removeAllViews();
                    viewList.clear();
                }
                List<String> imagesList = dataBean.getImages();
                if (imagesList.size() > 0) {
                    ll_point_container.setVisibility(View.VISIBLE);
                } else {
                    ll_point_container.setVisibility(View.INVISIBLE);
                }
                viewpager_count = imagesList.size();
                imageViews = new ImageView[viewpager_count];
                for (int i = 0; i < imagesList.size(); i++) {
                    View view_flipper = LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_index_top, null);
                    ImageView imageView = view_flipper.findViewById(R.id.banner);
                    Glide.with(this).load(imagesList.get(i)).into(imageView);
                    viewList.add(view_flipper);
                    View point_panel = LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_index_top_point, null);
                    imageViews[i] = point_panel.findViewById(R.id.point);
                    if (i == 0) {
                        imageViews[i].setAlpha(1f);
                    } else {
                        imageViews[i].setAlpha(0.5f);
                    }
                    ll_point_container.addView(point_panel);
                }
                viewPager.setAdapter(new MyViewPagerAdapter(viewList));
                viewPager.setCurrentItem(0);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        for (int i = 0; i < viewpager_count; i++) {
                            if (viewPager.getCurrentItem() == i) {
                                imageViews[i].setAlpha(1f);
                            } else {
                                imageViews[i].setAlpha(0.5f);
                            }
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });

                nav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Main.this, RideRouteCalculateActivity.class);
                        startActivity(intent);
                    }
                });
            } else {
                MyToast.showTheToast(this, msg);
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        myScrollView.A_1_close();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        myScrollView.A_1_close();
    }

    public boolean onMarkerClick(Marker marker) {
        for (int i = 0; i < dataBeanList.size(); i++) {
            String id = dataBeanList.get(i).getId();
            if (id.equals(marker.getTitle())) {
                PubFunction.local = new double[]{latitude, longitude};
                Double weidu = Double.valueOf(dataBeanList.get(i).getWeidu());
                Double jingdu = Double.valueOf(dataBeanList.get(i).getJingdu());
                PubFunction.marker = new double[]{weidu, jingdu};
                if (PubFunction.isConnect(this)) {
                    HttpMarkerInfo(id);
                    progressDialog.show();
                }
            }
        }
        return true;
    }

    @TargetApi(23)
    private void checkPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
            List<String> needRequestPermissionList = findDeniedPermissions(permissions);
            if (needRequestPermissionList != null && needRequestPermissionList.size() > 0) {
                String[] array = needRequestPermissionList.toArray(new String[0]);
                try {
                    Method method = getClass().getMethod("requestPermissions", String[].class, int.class);
                    method.invoke(this, array, PERMISSON_REQUESTCODE);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    //获取权限集中需要申请权限的列表
    @TargetApi(23)
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
            for (String perm : permissions) {
                if (checkMySelfPermission(perm) != PackageManager.PERMISSION_GRANTED ||
                        shouldShowMyRequestPermissionRationale(perm)) {
                    needRequestPermissionList.add(perm);
                }
            }
        }
        return needRequestPermissionList;
    }

    private int checkMySelfPermission(String perm) {
        try {
            Method method = getClass().getMethod("checkSelfPermission", String.class);
            return (Integer) method.invoke(this, perm);
        } catch (Throwable ignored) {
        }
        return -1;
    }

    private boolean shouldShowMyRequestPermissionRationale(String perm) {
        try {
            Method method = getClass().getMethod("shouldShowRequestPermissionRationale", String.class);
            return (Boolean) method.invoke(this, perm);
        } catch (Throwable ignored) {
        }
        return false;
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (requestCode == PERMISSON_REQUESTCODE) {
                if (!verifyPermissions(grantResults)) {
                    isNeedCheck = false;
                    showMissingPermissionDialog();
                }
            }
        }
    }

    @TargetApi(23)
    //检测是否所有的权限都已经授权
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //显式提示信息
    private void showMissingPermissionDialog() {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少必要权限\n\n请点击\"设置\"-\"权限\"-打开所需权限");
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    //启动应用的权限设置页面
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    //用户从权限设置页面返回，可能没有允许权限，需要再次检查权限
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {//申请权限
            if (Build.VERSION.SDK_INT >= 23) {
                isNeedCheck = true;
                checkPermissions(needPermissions);
            } else {
                startLocation();
            }
        }
    }

    @Override
    public void onFinish() {
        Log.i("052003", "onFinish");
    }

    @Override
    public void onCancel() {
        Log.i("052003", "onCancel");
        aMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(new CameraPosition(location1, 18, 0, 0)),
                999,
                Main.this);
    }

    public void resetSitesBackground() {
        site1_linearLayout.setBackgroundResource(R.drawable.site1_normal);
        site1_imageView.setBackgroundResource(R.drawable.site1);
        site1_textView.setTextColor(Color.parseColor("#eb5628"));

        site2_linearLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        site2_imageView.setBackgroundResource(R.drawable.site2);
        site2_textView.setTextColor(Color.parseColor("#79512a"));

        site3_linearLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        site3_imageView.setBackgroundResource(R.drawable.site3);
        site3_textView.setTextColor(Color.parseColor("#ff9320"));

        site4_linearLayout.setBackgroundResource(R.drawable.site4_normal);
        site4_imageView.setBackgroundResource(R.drawable.site4);
        site4_textView.setTextColor(Color.parseColor("#4f558f"));
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;//构造方法，参数是我们的页卡，这样比较方便。
        }

        //直接继承PagerAdapter，至少必须重写下面的四个方法，否则会报错
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (position < mListViews.size()) {
                container.removeView(mListViews.get(position));//删除页卡
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            //这个方法用来实例化页卡
            container.addView(mListViews.get(position), 0);//添加页卡
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();//返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
            return arg0 == arg1;//官方提示这样写
        }
    }
}