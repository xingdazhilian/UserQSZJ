package com.hellohuandian.userqszj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.fragment.AutenticationFragment;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.util.Util;

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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by hasee on 2017/6/6.
 */
@SuppressLint("Registered")
@EActivity(R.layout.main_authentication)
public class MainAuthentication extends BaseActivity {

    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView user_name, user_id, user_address;
    @ViewById
    LinearLayout user_card_1, user_card_2;
    @ViewById
    ImageView user_card_1_img, user_card_2_img;
    @ViewById
    Button submit;

    private String img_1_path = "", img_2_path = "";
    private String img_1_path_int = "", img_2_path_int = "";
    private String str_name = "", str_id = "";
    private String selectType = "身份证";
    private Uri imageUri;
    private File outputImage;
    private boolean isFirstPic = true;//判断是正面照还是反面照，用于照片信息错误后清空imageview

    //todo  ?? 上传正面使用的是uplaodAliyunFaceSuccessHandler，反面的是哪个？反面的相册用的是uplaodAliyunBackSuccessHandler，但是反面的拍照却用的是uplaodAliyunFaceSuccessHandler
    private Handler errorHandler, upLoadImage_1SuccessHandler, upLoadImage_2SuccessHandler, uplaodAliyunFaceSuccessHandler, uplaodAliyunFaceErrorHandler, uplaodAliyunBackSuccessHandler, uplaodAliyunBackErrorHandler;
    private Handler uplaodIdNameSuccessHandler, uplaodIdNameErrorHandler;

    @AfterViews
    void afterview() {
        activity = this;
        handler();
        lacksPermission();
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        errorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = msg.getData().getString("msg");
                MyToast.showTheToast(activity, str);
                progressDialog.dismiss();
            }
        };

        upLoadImage_1SuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = msg.getData().getString("data");
                try {

                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    img_1_path_int = jsonObject.getString("small_img");

                    str_name = user_name.getText().toString();
                    str_id = user_id.getText().toString();

                    if (!img_1_path_int.equals("") && !img_2_path_int.equals("")) {
                        HttpUplaodUserInfo(uid, str_name, selectType, str_id, img_1_path_int, img_2_path_int);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        upLoadImage_2SuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String data = msg.getData().getString("data");
                try {
                    JSONTokener jsonTokener = new JSONTokener(data);
                    JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                    img_2_path_int = jsonObject.getString("small_img");
                    str_name = user_name.getText().toString();
                    str_id = user_id.getText().toString();
                    if (!img_1_path_int.equals("") && !img_2_path_int.equals("")) {
                        HttpUplaodUserInfo(uid, str_name, selectType, str_id, img_1_path_int, img_2_path_int);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        uplaodAliyunFaceSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String data = msg.getData().getString("data");
//                    Util.d("tianyanyu1234", data);
                    if (data != null && (data.equals("Invalid Input - wrong category") || data.equals("Invalid Result - face results are all empty"))) {
                        MyToast.showTheToast(activity, "请上传正确的身份证照片！");
                        if (isFirstPic) {
                            user_card_1_img.setImageBitmap(null);
                            img_1_path = "";
                        } else {
                            user_card_2_img.setImageBitmap(null);
                            img_2_path = "";
                        }
                        progressDialog.dismiss();
                    } else {
                        JSONTokener jsonTokener = new JSONTokener(data);
                        JSONObject jsonObject_1 = (JSONObject) jsonTokener.nextValue();
                        JSONArray jsonArray_2 = jsonObject_1.getJSONArray("outputs");
                        JSONObject jsonObject_3 = jsonArray_2.getJSONObject(0);
                        JSONObject jsonObject_4 = jsonObject_3.getJSONObject("outputValue");
                        String jsonObject_5 = jsonObject_4.getString("dataValue");

                        JSONTokener jsonTokener_1 = new JSONTokener(jsonObject_5);
                        JSONObject jsonObject_6 = (JSONObject) jsonTokener_1.nextValue();

                        String type_str = jsonObject_6.getString("config_str");
                        if (type_str.indexOf("face") != -1) {
                            String name = jsonObject_6.getString("name");
                            String num = jsonObject_6.getString("num");
                            String address = jsonObject_6.getString("address");
                            user_name.setText(name);
                            user_id.setText(num);
                            user_address.setText(address);

                            HttpUplaodIdName(num, name);
                        } else {
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    if (isFirstPic) {
                        user_card_1_img.setImageBitmap(null);
                        img_1_path = "";
                    } else {
                        user_card_2_img.setImageBitmap(null);
                        img_2_path = "";
                    }
                    MyToast.showTheToast(activity, "请上传正确的身份证照片！");
                    progressDialog.dismiss();
                }
            }
        };

        uplaodAliyunFaceErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (isFirstPic) {
                    user_card_1_img.setImageBitmap(null);
                    img_1_path = "";
                } else {
                    user_card_2_img.setImageBitmap(null);
                    img_2_path = "";
                }
                progressDialog.dismiss();
                MyToast.showTheToast(activity, msg.getData().getString("msg"));
            }
        };

        uplaodAliyunBackSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressDialog.dismiss();
            }
        };

        uplaodAliyunBackErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressDialog.dismiss();
                MyToast.showTheToast(activity, "请上传正确的身份证照片！");
            }
        };

        uplaodIdNameSuccessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                MyToast.showTheToast(activity, msg.getData().getString("msg"));
                progressDialog.dismiss();
            }
        };

        uplaodIdNameErrorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                String data = msg.getData().getString("msg");
                MyToast.showTheToast(activity, data);
                progressDialog.dismiss();

                user_name.setText("");
                user_id.setText("");
                user_address.setText("");

            }
        };
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Click
    void user_card_1() {
        lacksPermission();
        AutenticationFragment autenticationFragment = AutenticationFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        autenticationFragment.setOnDialogItemClickListener(new AutenticationFragment.OnDialogItemClickListener() {
            @Override
            public void onLeftClick() {
                outputImage = new File(Environment.getExternalStorageDirectory(), "face.jpg");
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                try {
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    //通过FileProvider.getUriForFile获取URL，参数2应该与Provider在AndroidManifest.xml中定义的authorities标签一致
                    imageUri = FileProvider.getUriForFile(MainAuthentication.this, "com.hellohuandian.userqszj.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //MediaStore.ACTION_IMAGE_CAPTURE对应android.media.action.IMAGE_CAPTURE，用于打开系统相机
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 8083);
            }

            @Override
            public void onRightClick() {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 8081);
            }
        });
        autenticationFragment.show(fragmentTransaction, "AUTHEN");
    }

    @Click
    void user_card_2() {
        lacksPermission();
        AutenticationFragment autenticationFragment = AutenticationFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        autenticationFragment.setOnDialogItemClickListener(new AutenticationFragment.OnDialogItemClickListener() {
            @Override
            public void onLeftClick() {
                outputImage = new File(Environment.getExternalStorageDirectory(), "back.jpg");
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                try {
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    //通过FileProvider.getUriForFile获取URL，参数2应该与Provider在AndroidManifest.xml中定义的authorities标签一致
                    imageUri = FileProvider.getUriForFile(MainAuthentication.this, "com.hellohuandian.userqszj.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //MediaStore.ACTION_IMAGE_CAPTURE对应android.media.action.IMAGE_CAPTURE，用于打开系统相机
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 8084);

//                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                File f = new File(CreateFile.Image_DIR, "back.jpg");
//                Uri u = Uri.fromFile(f);
//                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
//                startActivityForResult(intent, 8084);
            }

            @Override
            public void onRightClick() {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 8082);
            }
        });
        autenticationFragment.show(fragmentTransaction, "AUTHEN");
    }

    @Click
    void submit() {
        str_name = user_name.getText().toString();
        str_id = user_id.getText().toString();
        if (str_name != "" && str_id != "" && img_1_path != "" && img_2_path != "") {
            UploadImage_A(img_1_path, uid, upLoadImage_1SuccessHandler, errorHandler);
            UploadImage_A(img_2_path, uid, upLoadImage_2SuccessHandler, errorHandler);
            progressDialog.show();
        } else {
            MyToast.showTheToast(this, "请先完善个人信息！");
        }
    }

    // 判断是否缺少权限
    private void lacksPermission() {
        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {

            int a = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (a == -1) {
                String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(activity, mPermissionList, 1);
            } else {
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 8081 && resultCode == RESULT_OK && null != data) {

            user_name.setText("");
            user_id.setText("");
            user_address.setText("");

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            img_1_path = cursor.getString(columnIndex);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
            options.inTempStorage = new byte[5 * 1024]; //设置16MB的临时存储空间（不过作用还没看出来，待验证）
            Bitmap bm = BitmapFactory.decodeFile(img_1_path, options);
            Matrix matrix = new Matrix();
            int width = bm.getWidth();
            int height = bm.getHeight();

            if (width < height) {
                matrix.postRotate(270); /*翻转90度*/
            }

            Bitmap img = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
            user_card_1_img.setImageBitmap(img);
            isFirstPic = true;
            HttpUplaodALIYUN httpUplaodALIYUN = new HttpUplaodALIYUN(img, "0", uplaodAliyunFaceSuccessHandler, uplaodAliyunFaceErrorHandler);
            httpUplaodALIYUN.start();
            cursor.close();
            progressDialog.show();
        }

        if (requestCode == 8082 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            img_2_path = cursor.getString(columnIndex);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
            options.inTempStorage = new byte[5 * 1024]; //设置16MB的临时存储空间（不过作用还没看出来，待验证）
            Bitmap bm = BitmapFactory.decodeFile(img_2_path, options);
            Matrix matrix = new Matrix();
            int width = bm.getWidth();
            int height = bm.getHeight();

            if (width < height) {
                matrix.postRotate(270); /*翻转90度*/
            }

            Bitmap img = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
            user_card_2_img.setImageBitmap(img);
            isFirstPic = false;
            HttpUplaodALIYUN httpUplaodALIYUN = new HttpUplaodALIYUN(img, "1", uplaodAliyunFaceSuccessHandler, uplaodAliyunBackErrorHandler);
            httpUplaodALIYUN.start();
            cursor.close();
            progressDialog.show();
        }

        if (requestCode == 8083 && resultCode == RESULT_OK) {
            user_name.setText("");
            user_id.setText("");
            user_address.setText("");

            img_1_path = outputImage.getAbsolutePath();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
            options.inTempStorage = new byte[5 * 1024]; //设置16MB的临时存储空间（不过作用还没看出来，待验证）
            Bitmap bm = BitmapFactory.decodeFile(img_1_path, options);
            Matrix matrix = new Matrix();
            int width = bm.getWidth();
            int height = bm.getHeight();

            if (width < height) {
                matrix.postRotate(270); /*翻转90度*/
            }
            Bitmap img = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
            user_card_1_img.setImageBitmap(img);
            isFirstPic = true;
            HttpUplaodALIYUN httpUplaodALIYUN = new HttpUplaodALIYUN(img, "0", uplaodAliyunFaceSuccessHandler, uplaodAliyunFaceErrorHandler);
            httpUplaodALIYUN.start();

            progressDialog.show();
        }

        if (requestCode == 8084 && resultCode == RESULT_OK) {

            img_2_path = outputImage.getAbsolutePath();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 5;//图片高宽度都为原来的二分之一，即图片大小为原来的大小的四分之一
            options.inTempStorage = new byte[5 * 1024]; //设置16MB的临时存储空间（不过作用还没看出来，待验证）
            Bitmap bm = BitmapFactory.decodeFile(img_2_path, options);
            Matrix matrix = new Matrix();
            int width = bm.getWidth();
            int height = bm.getHeight();

            if (width < height) {
                matrix.postRotate(270); /*翻转90度*/
            }

            Bitmap img = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
            user_card_2_img.setImageBitmap(img);
            isFirstPic = false;
            HttpUplaodALIYUN httpUplaodALIYUN = new HttpUplaodALIYUN(img, "1", uplaodAliyunFaceSuccessHandler, uplaodAliyunFaceErrorHandler);
            httpUplaodALIYUN.start();

            progressDialog.show();

        }
    }

    private void sendMessage(Handler handler, String str) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void sendMessage(Handler handler, String str, String data) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        bundle.putString("data", data);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Background
    void UploadImage_A(String imageUrl, String uid, Handler successHandler, Handler errorHandler) {
        String filepath = imageUrl;
        String urlStr = PubFunction.api + "Upload/upImage.html";
        Map<String, String> textMap = new HashMap<String, String>();
        textMap.put("uid", uid);
        Map<String, String> fileMap = new HashMap<String, String>();
        fileMap.put("upfile", filepath);
        String ret = formUpload(urlStr, textMap, fileMap);
        JSONTokener jsonTokener = new JSONTokener(ret);
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) jsonTokener.nextValue();
            String code = jsonObject.getString("status");
            String messageString = jsonObject.getString("msg");
            if (code.equals("1")) {
                if (jsonObject.has("data")) {
                    String data = jsonObject.getString("data");
                    sendMessage(successHandler, messageString, data);
                } else {
                    sendMessage(successHandler, messageString);
                }
            } else {
                sendMessage(errorHandler, messageString);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String formUpload(String urlStr, Map<String, String> textMap, Map<String, String> fileMap) {
        String res = "";
        HttpURLConnection conn = null;
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Map.Entry<String, String>> iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }

            // file
            if (fileMap != null) {
                Iterator<Map.Entry<String, String>> iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();

                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + "image/jpeg" + "\r\n\r\n");

                    out.write(strBuf.toString().getBytes());

                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
            }

            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();

            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            sendMessage(errorHandler, "发送POST请求出错。" + urlStr);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }

    @Background
    void HttpUplaodUserInfo(String uid, String name, String typer, String id, String front, String reverse) {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("real_name", name));
        dataList.add(new ParamTypeData("typer", typer));
        dataList.add(new ParamTypeData("card", id));
        dataList.add(new ParamTypeData("front", front));
        dataList.add(new ParamTypeData("reverse", reverse));

        new OkHttpConnect(activity, PubFunction.app + "User/identity.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onHttpUplaodUserInfo(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onHttpUplaodUserInfo(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    this.finish();
                }
                MyToast.showTheToast(activity, msg);
            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
            }
        }
    }

    @Background
    void HttpUplaodIdName(String num, String name) {
        List<ParamTypeData> dataList = new ArrayList<>();
        String path = "http://op.juhe.cn/idcard/query?key=138f605e59efab7fc41fabc38e568e6e&idcard=" + num + "&realname=" + name;
        new OkHttpConnect(activity, path, dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpUplaodIdName(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    @UiThread
    void onDataHttpUplaodIdName(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String code = jsonObject.getString("error_code");

                if (code.equals("0")) {
                    JSONObject dataObject = jsonObject.getJSONObject("result");
                    String res = dataObject.getString("res");
                    if (res.equals("1")) {
                        MyToast.showTheToast(activity, "身份证信息正确！");
                    } else {
                        MyToast.showTheToast(activity, "身份证信息错误！");
                        user_name.setText("");
                        user_id.setText("");
                        user_address.setText("");
                        if (isFirstPic) {
                            user_card_1_img.setImageBitmap(null);
                            img_1_path = "";
                        } else {
                            user_card_2_img.setImageBitmap(null);
                            img_2_path = "";
                        }
                    }
                } else {
                    String res = jsonObject.getString("reason");
                    MyToast.showTheToast(activity, "身份证信息错误！" + res);
                    user_name.setText("");
                    user_id.setText("");
                    user_address.setText("");
                    if (isFirstPic) {
                        user_card_1_img.setImageBitmap(null);
                        img_1_path = "";
                    } else {
                        user_card_2_img.setImageBitmap(null);
                        img_2_path = "";
                    }
                }

            } catch (Exception e) {
                MyToast.showTheToast(activity, "JSON：" + e.toString());
                user_name.setText("");
                user_id.setText("");
                user_address.setText("");
                if (isFirstPic) {
                    user_card_1_img.setImageBitmap(null);
                    img_1_path = "";
                } else {
                    user_card_2_img.setImageBitmap(null);
                    img_2_path = "";
                }
            }
        }
    }

//    private String handleImage(Uri uri) {
//        String imagePath = null;
//        if (DocumentsContract.isDocumentUri(this, uri)) {
//            // 如果是document 类型的 Uri，则通过document id处理
//            String docId = DocumentsContract.getDocumentId(uri);
//            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
//                String id = docId.split(":")[1];//解析出数字格式的id
//                String selection = MediaStore.Images.Media._ID + "=" + id;
//                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
//            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
//                Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
//                imagePath = getImagePath(contentUri, null);
//            }
//        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            //如果是content类型的Uri，则使用普通方式处理
//            imagePath = getImagePath(uri, null);
//        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            //如果是file类型的Uri，直接获取图片路径即可
//            imagePath = uri.getPath();
//        }
//        return imagePath;
//    }

//    private String getImagePath(Uri uri, String selection) {
//        String path = null;
//        // 通过Uri 和 selection 来获取真实的图片路径
//        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//            }
//            cursor.close();
//        }
//        return path;
//    }

}

class HttpUplaodALIYUN extends Thread {

    String url = "";
    String type = "";
    Bitmap bitmap = null;
    Handler success, error;

    public HttpUplaodALIYUN(String url, String type, Handler success, Handler error) {
        this.type = type;
        this.url = url;
        this.success = success;
        this.error = error;
    }

    public HttpUplaodALIYUN(Bitmap bitmap, String type, Handler success, Handler error) {
        this.type = type;
        this.bitmap = bitmap;
        this.success = success;
        this.error = error;
    }

    @Override
    public void run() {
        super.run();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url("http://dm-51.data.aliyun.com/rest/160601/ocr/ocr_idcard.json");
        requestBuilder.addHeader("Authorization", "APPCODE " + "46ddbe74949548999aeacd2cef617d7e");

        String str = "";
        if (!url.equals("")) {
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(url);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            Bitmap tem_bitmap = BitmapFactory.decodeStream(fis);

            //by tianyanyu 20190418
            //图片上传，需要压缩一下
            int requestWidth = (int) (1024 / 2.625);//计算1024像素的dp
            //这里传入的宽高是dp值
            Bitmap tem_bitmap = Util.decodeSampledBitmapFromFile(url, requestWidth, requestWidth);
            str = PubFunction.bitmapToBase64(tem_bitmap);
            tem_bitmap = null;
        } else {
            Bitmap tem_bitmap = bitmap;
            str = PubFunction.bitmapToBase64(tem_bitmap);
            tem_bitmap = null;
            bitmap = null;
        }
        String binaryToString = str;
        String body = "";
        if (type.equals("0")) {
            body = "{\"inputs\": [{\"image\": {\"dataType\": 50,\"dataValue\": \"" + binaryToString + "\"},\"configure\": {\"dataType\": 50,\"dataValue\": \"{\\\"side\\\":\\\"face\\\"}\"}}]}";
        } else if (type.equals("1")) {
            body = "{\"inputs\": [{\"image\": {\"dataType\": 50,\"dataValue\": \"" + binaryToString + "\"},\"configure\": {\"dataType\": 50,\"dataValue\": \"{\\\"side\\\":\\\"back\\\"}\"}}]}";
        }

        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        String requestBody = body;
        requestBuilder.post(RequestBody.create(mediaType, requestBody));
        Request request = requestBuilder.build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendMessage(error, "error：" + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result_str = response.body().string();
                sendMessage(success, "返回成功", result_str);
            }
        });


//        String path = "http://dm-51.data.aliyun.com/rest/160601/ocr/ocr_idcard.json";
//        HttpPost httpPost = new HttpPost(path);
//
//        Header header1 = new Header() {
//            @Override
//            public String getName() {
//                return "Authorization";
//            }
//
//            @Override
//            public String getValue() {
//                return "APPCODE " + "46ddbe74949548999aeacd2cef617d7e";
//            }
//
//            @Override
//            public HeaderElement[] getElements() throws ParseException {
//                return new HeaderElement[0];
//            }
//        };
//        httpPost.addHeader(header1);
//
//        String str = "";
//        if (!url.equals("")) {
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(url);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            Bitmap tem_bitmap = BitmapFactory.decodeStream(fis);
//            str = PubFunction.bitmapToBase64(tem_bitmap);
//            tem_bitmap = null;
//        } else {
//            Bitmap tem_bitmap = bitmap;
//            str = PubFunction.bitmapToBase64(tem_bitmap);
//            tem_bitmap = null;
//            bitmap = null;
//        }
//
//        String binaryToString = str;
//        String body = "";
//        if (type.equals("0")) {
//            body = "{\"inputs\": [{\"image\": {\"dataType\": 50,\"dataValue\": \"" + binaryToString + "\"},\"configure\": {\"dataType\": 50,\"dataValue\": \"{\\\"side\\\":\\\"face\\\"}\"}}]}";
//
//        } else if (type.equals("1")) {
//            body = "{\"inputs\": [{\"image\": {\"dataType\": 50,\"dataValue\": \"" + binaryToString + "\"},\"configure\": {\"dataType\": 50,\"dataValue\": \"{\\\"side\\\":\\\"back\\\"}\"}}]}";
//        }
//        try {
//            httpPost.setEntity(new StringEntity(body, "utf-8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpResponse httpResponse = client.execute(httpPost);
//            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String result = EntityUtils.toString(httpResponse.getEntity());
//
//
//                JSONTokener jsonTokener = new JSONTokener(result);
//                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
//
//                sendMessage(success, "返回成功", jsonObject.toString());
//            } else if (httpResponse.getStatusLine().getStatusCode() == 400) {
//                sendMessage(error, "上传图片不清晰，请重新上传");
//            } else {
//                sendMessage(error, "服务器错误：HttpUplaodUserInfo");
//            }
//        } catch (Exception e) {
//            System.out.println(e.toString() + "aaaaaa");
//            sendMessage(error, "json解析错误：HttpUplaodUserInfo");
//        }
    }

    private void sendMessage(Handler handler, String str) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void sendMessage(Handler handler, String str, String data) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("msg", str);
        bundle.putString("data", data);
        message.setData(bundle);
        handler.sendMessage(message);
    }

}
