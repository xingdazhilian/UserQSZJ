package com.hellohuandian.userqszj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.ProgressDialog;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.Unit;
import com.hellohuandian.userqszj.service.BluetoothLeService;
import com.hellohuandian.userqszj.util.Util;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hasee on 2017/6/6.
 */
@SuppressLint("Registered")
@EActivity(R.layout.main_bluetooth)
public class MainBlueTooth extends BaseActivity {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    // 蓝牙扫描时间
    private static final long SCAN_PERIOD = 10000;
    private final static String TAG = MainBlueTooth.class.getSimpleName();
    //设置权限
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    //蓝牙service,负责后台的蓝牙服务
    private static BluetoothLeService mBluetoothLeService;
    //蓝牙特征值
    private static BluetoothGattCharacteristic target_chara = null;
    @ViewById
    LinearLayout page_return, count_panel;
    @ViewById
    TextView send, coin_1, updata;
    // 蓝牙适配器
    BluetoothAdapter mBluetoothAdapter;
    /**
     * @param @param rev_string(接受的数据)
     * @return void
     * @throws
     * @Title: displayData
     * @Description: TODO(接收到的数据在scrollview上显示)
     */
    int out_time_count = 30;
    String countString = "0";
    private Handler mHandler;
    //蓝牙名字
    private String mDeviceName;
    //蓝牙地址
    private String mDeviceAddress;
    /* BluetoothLeService绑定的回调函数 */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            // 根据蓝牙地址，连接设备
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    //蓝牙连接状态
    private boolean mConnected = false;
    private String status = "disconnected";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ProgressDialog progressDialog;
    private Handler mhandler = new Handler();
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {
        // 2.重写消息处理函数
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // 判断发送的消息
                case 1: {
                    // 更新View
                    String state = msg.getData().getString("connect_state");
                    MyToast.showTheToast(activity, state);
                    break;
                }
            }
        }
    };
    private int is_charge = 0;
    private int is_update = 0;
    private ArrayList<byte[]> dataList = null;
    /**
     * 广播接收器，负责接收BluetoothLeService类发送的数据
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//Gatt连接成功
                mConnected = true;
                status = "链接成功！";
                //更新连接状态
                updateConnectionState(status);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {//Gatt连接失败
                mConnected = false;
                status = "链接失败！";
                //更新连接状态
                updateConnectionState(status);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //发现GATT服务器
                // Show all the supported services and characteristics on the
                // user interface.
                //获取设备的所有蓝牙服务
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //有效数据
                //处理发送过来的数据
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    /**
     * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
     **/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            // TODO Auto-generated method stub
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String device_name = "";
                    if (device.getName() != null) {
                        device_name = device.getName();
                    }
                    if (device_name.equals("HC-08")) {
                        scanLeDevice(false);
                        progressDialog.dismiss();
                        MyToast.showTheToast(activity, "已经发现设备！");
                        String device_address = device.getAddress();
                        mDeviceAddress = device_address;
                        mDeviceName = device_name;
                        ConnectBlueTooth();
                    }
                }
            });
        }
    };

    /* 意图过滤器 */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @AfterViews
    void afterViews() {
        init();
        if (PubFunction.isConnect(this)) {
            HttpLifeUtimes();
        } else {
            MyToast.showTheToast(this, "请检查网络连接，否则无法为充值充电次数");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除广播接收器
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
    }

    // Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
    @Override
    protected void onResume() {
        super.onResume();
        //绑定广播接收器
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            //根据蓝牙地址，建立连接
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    private void init() {
        //设置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        progressDialog = new ProgressDialog(activity);
        progressDialog.setText("正在检测设备,请稍候...");
        progressDialog.show();
        init_ble();
        mHandler = new Handler();
        scanLeDevice(true);
    }

    /**
     * @param
     * @return void
     * @throws
     * @Title: init_ble
     * @Description: TODO(初始化蓝牙)
     */
    private void init_ble() {
        // 手机硬件支持蓝牙
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        // 获取手机本地的蓝牙适配器
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 打开蓝牙权限
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    /**
     * @param enable (扫描使能，true:扫描开始,false:扫描停止)
     * @return void
     * @throws
     * @Title: scanLeDevice
     * @Description: TODO(扫描蓝牙设备)
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("SCAN", "stop.....................");
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    progressDialog.dismiss();
                    if (target_chara == null) {
                        MyToast.showTheToast(activity, "未发现充电器设备");
                    }
                }
            }, SCAN_PERIOD);
            /* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
            Log.i("SCAN", "begin.....................");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            Log.i("Stop", "stoping................");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 启动蓝牙
     **/
    private void ConnectBlueTooth() {
        MyToast.showTheToast(activity, "正在链接设备，请稍候...");
        /* 启动蓝牙service */
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /* 更新连接状态 */
    private void updateConnectionState(String status) {
        Message msg = new Message();
        msg.what = 1;
        Bundle b = new Bundle();
        b.putString("connect_state", status);
        msg.setData(b);
        //将连接状态更新的UI的textview上
        myHandler.sendMessage(msg);
        System.out.println("connect_state:" + status);
    }

    /**
     * @param
     * @return void
     * @throws
     * @Title: displayGattServices
     * @Description: TODO(处理蓝牙服务)
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";
        // 服务数据,可扩展下拉列表的第一级数据
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        // 特征数据（隶属于某一级服务下面的特征值集合）
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<>();
        // 部分层次，所有特征值集合
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            // 获取服务列表
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            // 查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。
            gattServiceData.add(currentServiceData);
            System.out.println("Service uuid:" + uuid);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            // 从当前循环所指向的服务中读取特征值列表
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();
            // Loops through available Characteristics.
            // 对于当前循环所指向的服务中的每一个特征值
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();

                if (gattCharacteristic.getUuid().toString()
                        .equals(HEART_RATE_MEASUREMENT)) {
                    // 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mhandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mBluetoothLeService
                                    .readCharacteristic(gattCharacteristic);
                        }
                    }, 200);
                    // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                    target_chara = gattCharacteristic;
                    // 设置数据内容
                    // 往蓝牙模块写入数据
                    // mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor descriptor : descriptors) {
                    System.out.println("---descriptor UUID:"
                            + descriptor.getUuid());
                    // 获取特征值的描述
                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);
                    // mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
                    // true);
                }
                gattCharacteristicGroupData.add(currentCharaData);
            }
            // 按先后顺序，分层次放入特征值集合中，只有特征值
            mGattCharacteristics.add(charas);
            // 构件第二级扩展列表（服务下面的特征值）
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private void displayData(String returnStr) {
        //返回的数据处理
        String str = returnStr;
        int count = str.length() / 2;
        String[] RX = new String[count];
        for (int i = 0; i < count; i++) {
            RX[i] = str.substring(i * 2, (i + 1) * 2);
        }
        System.out.println("bluetooth_return : " + returnStr);
        //设置充电次数
        if (RX.length == 25) {
            if (RX[0].equals("A5") && RX[1].equals("19") && RX[2].equals("01")) {
                byte[] a = new byte[]{(byte) 0xA5, (byte) 0x0A, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x80, (byte) 0x03, (byte) 0x16, (byte) 0x9D, (byte) 0x5A};
                target_chara.setValue(a);
                mBluetoothLeService.writeCharacteristic(target_chara);
                System.out.println("对接成功！");
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (is_charge == 0) {
                            if (target_chara == null) {
                                MyToast.showTheToast(activity, "未链接到设备！");
                            } else {
                                progressDialog = new ProgressDialog(activity);
                                progressDialog.setText("正在设置充电次数，请稍候......");
                                progressDialog.setcanCancele(false);
                                progressDialog.show();
                                is_charge = 1;
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        while (out_time_count > 0) {
                                            out_time_count = out_time_count - 1;
                                            try {
                                                sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (out_time_count == 1) {
                                                progressDialog.dismiss();
                                            }
                                        }
                                    }
                                };
                                thread.start();
                            }
                        } else {
                            MyToast.showTheToast(activity, "正在设置充电次数，请勿重复提交");
                        }
                    }
                });
                send.setBackgroundResource(R.drawable.button_corners_green_radius_5);
            }
        }
        //下发充电器次数
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("05") && RX[3].equals("00")) {
                if (is_charge == 1) {
                    target_chara.setValue(send_count(countString));
                    mBluetoothLeService.writeCharacteristic(target_chara);
                    progressDialog.dismiss();
                    is_charge = 0;
                    out_time_count = -1;
                }
            }
        }
        //设置充电器成功
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("07") && RX[3].equals("00")) {
                MyToast.showTheToast(activity, "充电器设置成功！");
                HttpLifeAffirm();
            }
        }
        //显示次数
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("03") && RX[3].equals("00")) {
                //设置次数相关
                String count_str = RX[5];
                int count_int = Integer.parseInt(count_str, 16);
                count_panel.setVisibility(View.VISIBLE);
                coin_1.setText(count_int + "");
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyToast.showTheToast(activity, "充电器还存在剩余次数，请勿进行设置！");
                    }
                });
                //升级相关
                updata.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (is_update == 0) {
                            if (target_chara == null) {
                                MyToast.showTheToast(activity, "未链接到设备！");
                            } else {
                                progressDialog = new ProgressDialog(activity);
                                progressDialog.setText("正在升级充电器，请稍候......");
                                progressDialog.setcanCancele(false);
                                progressDialog.show();
                                is_update = 1;
                            }
                        } else {
                            MyToast.showTheToast(activity, "正在设置充电次数，请勿重复提交");
                        }
                    }
                });
                updata.setBackgroundResource(R.drawable.button_corners_green_radius_5);
                if (is_update == 0) {
                } else if (is_update == 1) {
                    is_update = 0;
                    String filePath = Environment.getExternalStorageDirectory() + "/HelloElectricity/demo.bin";
                    File file = new File(filePath);
                    long filesize = file.length();
                    long a_s = filesize / (256);
                    long b_s = filesize % (256);
                    dataList = new ArrayList<>();
                    try {
                        FileInputStream is = new FileInputStream(file);
                        byte[] buffer = new byte[64];
                        int length = 0;
                        while ((length = is.read(buffer)) > 0) {

                            byte[] hexData = new byte[length];
                            for (int i = 0; i < length; i++) {
                                hexData[i] = buffer[i];
                            }
                            dataList.add(hexData);
                            Arrays.fill(buffer, (byte) 0x00);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] a = send_filesize((int) a_s, (int) b_s);
                    System.out.println("bluetooth_send : " + bytesToHex(a));
                    target_chara.setValue(a);
                    mBluetoothLeService.writeCharacteristic(target_chara);
                }
            }
        }
        //文件太大 终止升级 停止发送06
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("09") && RX[3].equals("00")) {

            }
        }
        //可以升级，准备发送bin文件包，09与0B都在发完06后的50ms-200ms之间收到
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("0B") && RX[3].equals("00")) {
            }
        }
        //收到0B后的50ms-150ms之间回收到带有文件包序号的下发请求。
        if (RX.length == 10) {
            if (RX[0].equals("A5") && RX[1].equals("0A") && RX[2].equals("0D") && RX[3].equals("02")) {
                final String c_h = RX[4];
                final String c_l = RX[5];
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        byte[] a = send_filedata(c_h, c_l);
                        System.out.println("bluetooth_send : " + bytesToHex(a));
                        int item_order = (a.length / 20) + 1;
                        for (int i = 0; i < item_order; i++) {
                            if (i == item_order - 1) {
                                byte[] item_a = new byte[14];
                                for (int j = 0; j < item_a.length; j++) {
                                    item_a[j] = a[(i * 20) + j];
                                }
                                target_chara.setValue(item_a);
                                mBluetoothLeService.writeCharacteristic(target_chara);
                            } else {
                                byte[] item_a = new byte[20];
                                for (int j = 0; j < item_a.length; j++) {
                                    item_a[j] = a[(i * 20) + j];
                                }
                                target_chara.setValue(item_a);
                                mBluetoothLeService.writeCharacteristic(target_chara);
                            }
                            try {
                                sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                thread.start();
            }
        }
    }

    @Click
    void send() {
        if (is_charge == 0) {
            if (target_chara == null) {
                MyToast.showTheToast(activity, "未链接到设备！");
            } else {
                MyToast.showTheToast(activity, "正在配置充电器，请稍候...");
            }
        } else {
            MyToast.showTheToast(activity, "正在设置充电次数，请勿重复提交");
        }
    }

    @Click
    void updata() {
        if (is_charge == 0) {
            if (target_chara == null) {
                MyToast.showTheToast(activity, "未链接到设备！");
            } else {
                MyToast.showTheToast(activity, "正在配置充电器，请稍候...");
            }
        } else {
            MyToast.showTheToast(activity, "正在升级充电器，请勿重复提交");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    private byte[] send_count(String count) {
        byte[] message_b = new byte[]{0, 0, 0, 0, 0, 0, 0};
        message_b[0] = (byte) 0xA5;
        message_b[1] = (byte) 0x0A;
        message_b[2] = (byte) 0x04;
        message_b[3] = (byte) 0x00;
        message_b[4] = (byte) 0x02;
        message_b[5] = (byte) Integer.parseInt(count, 16);
        message_b[6] = (byte) 0x03;
        String str = Unit.getCRC(message_b);
        while (true) {
            if (str.length() < 4) {
                str = "0" + str;
            } else {
                break;
            }
        }
        String str_06 = str.substring(0, 2);
        String str_07 = str.substring(2, 4);
        byte[] message_c = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        message_c[0] = (byte) 0xA5;
        message_c[1] = (byte) 0x0A;
        message_c[2] = (byte) 0x04;
        message_c[3] = (byte) 0x00;
        message_c[4] = (byte) 0x02;
        message_c[5] = (byte) Integer.parseInt(count, 16);
        message_c[6] = (byte) 0x03;
        message_c[7] = (byte) Integer.parseInt(str_07, 16);
        message_c[8] = (byte) Integer.parseInt(str_06, 16);
        message_c[9] = (byte) 0x5A;
        return message_c;
    }

    private byte[] send_filesize(int size_h, int size_l) {
        byte[] message_b = new byte[]{0, 0, 0, 0, 0, 0, 0};
        message_b[0] = (byte) 0xA5;
        message_b[1] = (byte) 0x0A;
        message_b[2] = (byte) 0x06;
        message_b[3] = (byte) 0x02;
        message_b[4] = (byte) size_h;
        message_b[5] = (byte) size_l;
        message_b[6] = (byte) 0x03;
        String str = Unit.getCRC(message_b);
        while (true) {
            if (str.length() < 4) {
                str = "0" + str;
            } else {
                break;
            }
        }
        String str_06 = str.substring(0, 2);
        String str_07 = str.substring(2, 4);
        byte[] message_c = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        message_c[0] = (byte) 0xA5;
        message_c[1] = (byte) 0x0A;
        message_c[2] = (byte) 0x06;
        message_c[3] = (byte) 0x02;
        message_c[4] = (byte) size_h;
        message_c[5] = (byte) size_l;
        message_c[6] = (byte) 0x03;
        message_c[7] = (byte) Integer.parseInt(str_07, 16);
        message_c[8] = (byte) Integer.parseInt(str_06, 16);
        message_c[9] = (byte) 0x5A;
        return message_c;
    }

    private byte[] send_filedata(String count_h, String count_l) {
        //获得要发的第几包
        String count_str = count_h + count_l;
        int count_int = Integer.parseInt(count_str, 16);
        //获取包的数据
        byte[] byteData = dataList.get((count_int / 64));
        //获取包的有效长度
        int dataSize = byteData.length;
        System.out.println("bluetooth_send :" + bytesToHex(byteData) + "     " + dataSize);
        byte[] message_b = new byte[71];
        Arrays.fill(message_b, (byte) 0xff);
        message_b[0] = (byte) 0xa5;
        message_b[1] = (byte) dataSize;
        message_b[2] = (byte) 0x08;
        message_b[3] = (byte) Integer.parseInt(count_h, 16);
        message_b[4] = (byte) Integer.parseInt(count_l, 16);
        message_b[5] = (byte) 0x02;
        for (int i = 0; i < dataSize; i++) {
            message_b[6 + i] = byteData[i];
        }
        message_b[70] = (byte) 0x03;
        String str = Unit.getCRC(message_b);
        while (true) {
            if (str.length() < 4) {
                str = "0" + str;
            } else {
                break;
            }
        }
        String str_06 = str.substring(0, 2);
        String str_07 = str.substring(2, 4);
        byte[] message_c = new byte[74];
        Arrays.fill(message_c, (byte) 0xff);
        message_c[0] = (byte) 0xa5;
        message_c[1] = (byte) dataSize;
        message_c[2] = (byte) 0x08;
        message_c[3] = (byte) Integer.parseInt(count_h, 16);
        message_c[4] = (byte) Integer.parseInt(count_l, 16);
        message_c[5] = (byte) 0x02;
        for (int i = 0; i < dataSize; i++) {
            message_c[6 + i] = byteData[i];
        }
        message_c[70] = (byte) 0x03;
        message_c[71] = (byte) Integer.parseInt(str_07, 16);
        message_c[72] = (byte) Integer.parseInt(str_06, 16);
        message_c[73] = (byte) 0x5A;
        return message_c;
    }

    /**
     * http接口：http://app.halouhuandian.com/Life/utimes.html   获取当前版本号
     */
    @Background
    void HttpLifeUtimes() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        new OkHttpConnect(activity, PubFunction.app + "Life/utimes.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLifeUtimes(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpLifeUtimes(String response, String type) {
//        Util.d("052801", response);
        if (type.equals("0")) {
            MyToast.showTheToast(this, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.getString("status");
                String msg = jsonObject.getString("msg");
                if (status.equals("1")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    String bt_cycles = dataObject.getString("bt_cycles");
                    String bp_cycles = dataObject.getString("bp_cycles");
                    double count = Double.parseDouble(bp_cycles);
                    //如果uid的可充电次数大于100次，那么单次只能充值100次
                    if (count >= 100) {
                        countString = "64";
                    } else if (count > 0 && count < 100) {
                        countString = Integer.toHexString((int) count);
                    } else {
                        countString = "0";
                    }
//                    Util.d("countString", countString);
                } else {
                    MyToast.showTheToast(this, msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * http接口：http://app.halouhuandian.com/Life/affirm.html   获取当前版本号
     */
    @Background
    void HttpLifeAffirm() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("times", Integer.parseInt(countString, 16) + ""));
//        Util.d("052803", Integer.parseInt(countString, 16) + "");
        new OkHttpConnect(activity, PubFunction.app + "Life/affirm.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpLifeAffirm(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpLifeAffirm(String response, String type) {
        System.out.println(response);
    }
}