package com.hellohuandian.userqszj.http;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.hellohuandian.userqszj.pub.Md5;


public class HeaderTypeData {


    public static String[][] HEADER_Whit_APTK_APUD(Activity activity, String uid) {
        String[][] HEADER_Whit_APTK_APUD = new String[][]{{"aptk", new Md5().getDateToken()}, {"version", getLocalVersionName(activity)}, {"verName", getLocalVersion(activity) + ""}, {"osName", "Android"}, {"apud", uid}, {"proname", "app"}};
        Log.i("052006", "apud:" + uid);

        return HEADER_Whit_APTK_APUD;
    }

    public static String[][] HEADER_Whit_APTK_APUD_PRO(Activity activity, String uid) {
        String[][] HEADER_Whit_APTK_APUD = new String[][]{{"aptk", new Md5().getDateToken()}, {"version", getLocalVersionName(activity)}, {"verName", getLocalVersion(activity) + ""}, {"osName", "Android"}, {"apud", uid}, {"proname", "app"}};
        return HEADER_Whit_APTK_APUD;
    }

    public static String[][] HEADER_Whit_APTK(Activity activity) {
        String[][] HEADER_Whit_APTK = new String[][]{{"aptk", new Md5().getDateToken()}, {"version", getLocalVersionName(activity)}, {"verName", getLocalVersion(activity) + ""}, {"osName", "Android"}, {"proname", "app"}};
        return HEADER_Whit_APTK;
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

    //获得版本号
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


}
