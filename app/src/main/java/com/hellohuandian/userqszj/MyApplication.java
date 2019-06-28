package com.hellohuandian.userqszj;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyApplication extends Application {
    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * 初始化腾讯Bugly
         */
        Context context = getApplicationContext();
        //获取当前包名
        String packageName = context.getPackageName();
        //获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        //设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        //初始化Bugly
        CrashReport.initCrashReport(context, "6a0a4a0e67", false, strategy);
        //制造一个Crash测试Bugly
//        CrashReport.testJavaCrash();
    }
}
