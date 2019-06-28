package com.hellohuandian.userqszj;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

public class DownLoadAndInstallApp {


    /**
     * 新版本更新
     */
    String saveName = "myApp.apk";
    DownloadManager downloadManager;
    long mTaskId = 0;
    private Activity activity;
    //广播接受者，接收下载状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //检查下载状态
            checkDownloadStatus();
        }
    };

    public DownLoadAndInstallApp(Activity activity, String versionUrl, String versionName) {
        this.activity = activity;
        downloadAPK(versionUrl, versionName);
    }

    //使用系统下载器下载
    private void downloadAPK(String versionUrl, String versionName) {

        deleteFile();

        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载
        // 设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "myApp.apk");
        //request.setDestinationInExternalFilesDir(),也可 以自己制定下载路径
        // 将下载请求加入下载队列
        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);
        //注册广播接收者，监听下载状态
        activity.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public boolean deleteFile() {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + saveName;
        File file = new File(downloadPath);
        String fileName = saveName;
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);
        //筛选下载任务，传入任务ID，可变参数
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    System.out.println(">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    System.out.println(">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    System.out.println(">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    System.out.println(">>>下载完成");

                    String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + saveName;
                    File f = new File(downloadPath);
                    installAPK(f);

                    break;
                case DownloadManager.STATUS_FAILED:
                    System.out.println(">>>下载失败");
                    break;
            }
        }
    }


    //下载到本地后执行安装
    protected void installAPK(File file_1) {

        if (!file_1.exists()) {
            Toast.makeText(activity, "未找到更新文件！", Toast.LENGTH_LONG).show();
            return;
        } else {

            Toast.makeText(activity, "正在更新！", Toast.LENGTH_LONG).show();

            if (file_1 != null) {

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myApp.apk");
                Intent intent = new Intent(Intent.ACTION_VIEW); // 由于没有在Activity环境下启动Activity,设置下面的标签
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上 //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致 参数3 共享的文件
                    Uri apkUri = FileProvider.getUriForFile(activity, "com.hellohuandian.userqszj.fileprovider", file); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                } else {
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                }
                activity.startActivity(intent);
            }


        }
    }

}
