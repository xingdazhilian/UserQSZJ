package com.hellohuandian.userqszj.pub;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PubFunction {
    public static String www = "https://huandian.halouhuandian.com/";
    public static String app = "https://app.halouhuandian.com/";
    public static String api = "https://api.halouhuandian.com/";

//    public static String www = "https://testhuandian.halouhuandian.com/";
//    public static String app = "https://testapp.halouhuandian.com/";
//    public static String api = "https://testapi.halouhuandian.com/";

    public static String car_ID = "";
    public static double[] marker = null;// 目标坐标地址
    public static double[] local = null;// 自己坐标地址
    //数字转大写
    static String[] units = {"", "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿", "百亿", "千亿", "万亿"};
    static char[] numArray = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'};
    private static int LOG_MAXLENGTH = 2000;

    //dip和px互转
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    //bitmap转base64
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String formatInteger(int num) {
        char[] val = String.valueOf(num).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            String m = val[i] + "";
            int n = Integer.valueOf(m);
            boolean isZero = n == 0;
            String unit = units[(len - 1) - i];
            if (isZero) {
                if ('0' == val[i - 1]) {
                    continue;
                } else {
                    sb.append(numArray[n]);
                }
            } else {
                sb.append(numArray[n]);
                sb.append(unit);
            }
        }
        return sb.toString();
    }

    //检查是否有网络
    public static boolean isConnect(Context context) {
        boolean _isConnect = false;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if (network != null) {
            _isConnect = conManager.getActiveNetworkInfo().isAvailable();
        }
        if (!_isConnect) {
            MyToast.showTheToast(context, "无网络链接，请检查您的网络设置！");
        }

        return _isConnect;
    }

    public static boolean isConnect(Context context, boolean b) {
        boolean _isConnect = false;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if (network != null) {
            _isConnect = conManager.getActiveNetworkInfo().isAvailable();
        }

        if (b == true) {
            if (!_isConnect) {
                MyToast.showTheToast(context, "无网络链接，请检查您的网络设置！");
            }
        }
        return _isConnect;
    }

    //设置girdview高度
    public static void setGridViewHeight(GridView gridView, int size, int numColumns) {
        try {
            ListAdapter adapter = gridView.getAdapter();

            int a = size;
            int b = a / numColumns;
            int c = a % numColumns;

            if (c == 0) {
                a = b;
            } else {
                a = b + 1;
            }

            int row = a;

            View listItem = adapter.getView(0, null, gridView);
            if (listItem == null)
                return;
            listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            listItem.measure(0, 0);
            int totalHeight = listItem.getMeasuredHeight() * row + (gridView.getVerticalSpacing() * (row - 1));
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //动态设置listview高度
    public static void setListViewHeight(ListView listView) { //获取listView的adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0; //listAdapter.getCount()返回数据项的数目
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));


        listView.setLayoutParams(params);
    }

    //获得版本名
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext().getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext().getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    //给HttpPost塞header头文件
    public static void setHandlerWhit_APTK(final Activity activity,
                                           final String type, HttpPost httpPost) {

        Header header1 = new Header() {
            @Override
            public String getName() {
                return "aptk";
            }

            @Override
            public String getValue() {
                return new Md5().getDateToken();
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header1);


        Header header2 = new Header() {
            @Override
            public String getName() {
                return "verName";
            }

            @Override
            public String getValue() {
                return getLocalVersionName(activity);
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header2);


        Header header3 = new Header() {
            @Override
            public String getName() {
                return "version";
            }

            @Override
            public String getValue() {
                return getLocalVersion(activity) + "";
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header3);

        Header header4 = new Header() {
            @Override
            public String getName() {
                return "osName";
            }

            @Override
            public String getValue() {
                return "Android";
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header4);

        Header header5 = new Header() {
            @Override
            public String getName() {
                return "proName";
            }

            @Override
            public String getValue() {
                return type;
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header5);
    }

    public static void setHandlerWhit_APTK_APUD(final Activity activity, final String type,
                                                final String uid, HttpPost httpPost) {

        Header header1 = new Header() {
            @Override
            public String getName() {
                return "aptk";
            }

            @Override
            public String getValue() {
                return new Md5().getDateToken();
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header1);


        Header header2 = new Header() {
            @Override
            public String getName() {
                return "version";
            }

            @Override
            public String getValue() {
                return getLocalVersionName(activity);
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header2);


        Header header3 = new Header() {
            @Override
            public String getName() {
                return "verName";
            }

            @Override
            public String getValue() {
                return getLocalVersion(activity) + "";
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header3);

        Header header4 = new Header() {
            @Override
            public String getName() {
                return "osName";
            }

            @Override
            public String getValue() {
                return "Android";
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header4);

        Header header6 = new Header() {
            @Override
            public String getName() {
                return "apud";
            }

            @Override
            public String getValue() {
                return uid;
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        };
        httpPost.addHeader(header6);

    }

    /*
     * 保存文件，文件名为当前日期
     */
    public static void SaveBitmap(Activity activity, Bitmap bitmap, String bitName) {
        String fileName;
        File file;
        if (Build.BRAND.equals("Xiaomi")) { // 小米手机
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else { // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName;
        }
        file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file); // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                MediaStore.Images.Media.insertImage(activity.getContentResolver(), file.getAbsolutePath(), bitName, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } // 发送广播，通知刷新图库的显示 this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName))); }
    }

    public static void hide_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void show_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void e(String TAG, String msg) {
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 100; i++) { //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                Log.e(TAG + i, msg.substring(start, end));
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                Log.e(TAG, msg.substring(start, strLength));
                break;
            }
        }
    }
}
