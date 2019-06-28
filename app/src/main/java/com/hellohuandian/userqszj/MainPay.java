package com.hellohuandian.userqszj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.hellohuandian.userqszj.http.HeaderTypeData;
import com.hellohuandian.userqszj.http.OkHttpConnect;
import com.hellohuandian.userqszj.http.ParamTypeData;
import com.hellohuandian.userqszj.pub.MyToast;
import com.hellohuandian.userqszj.pub.ProgressDialog;
import com.hellohuandian.userqszj.pub.PubFunction;
import com.hellohuandian.userqszj.pub.zhifubao.PayResult;
import com.hellohuandian.userqszj.pub.zhifubao.SignUtils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

class MainPay {
    //    public static String RSA_PUBLIC = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    //支付宝信息
    private static String PARTNER = "";
    private static String SELLER = "";
    private static String RSA_PRIVATE = "";
    //订单信息
    private String pay_type;
    private String gid;
    private String uid;
    //其他参数
    private Activity activity;
    private ProgressDialog progressDialog;
    private Handler mHandler, succcessHandler, succcessNoDataHandler, errorHandler;
    private String price = "";
    private String trade_sn = "";
    private String notify_url = "";
    private String name = "";

    MainPay(Activity activity, String pay_type, String gid, String uid) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_1();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    MainPay(Activity activity, String pay_type, String gid, String uid, int a) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_2();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    //钱包支付
    MainPay(Activity activity, String pay_type, String gid, String uid, int a, int b) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_3();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    //购买支付
    MainPay(Activity activity, String pay_type, String gid, String uid, int a, int b, int c) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_4();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    //租赁支付
    MainPay(Activity activity, String pay_type, String gid, String uid, int a, int b, int c, int d) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_5();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    //未支付订单
    MainPay(Activity activity, String pay_type, String gid, String uid, int a, int b, int c, int d, int e) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_6();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    //骑士币支付
    MainPay(Activity activity, String pay_type, String gid, String uid, int a, int b, int c, int d, int e, int f) {
        this.activity = activity;
        this.pay_type = pay_type;
        this.gid = gid;
        this.uid = uid;
        handler();
        if (PubFunction.isConnect(activity)) {
            HttpGetOrderInfo_7();
            progressDialog = new ProgressDialog(activity);
            progressDialog.show();
        }
    }

    @SuppressLint("HandlerLeak")
    private void handler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
//                Util.d("aliresult1", msg.toString());
                switch (msg.what) {
                    case SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);
                        /**
                         * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                         * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                         * docType=1) 建议商户依赖异步通知
                         */
                        String resultInfo = payResult.getResult();// 同步返回需要验证的信息
//                        Util.d("033001", resultInfo);
                        System.out.println(resultInfo);

                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            MyToast.showTheToast(activity, "支付成功！");
                            activity.startActivity(new Intent(activity, MainWallet_.class));
                            activity.finish();
                        } else {
                            // 判断resultStatus 为非"9000"则代表可能支付失败
                            // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            if (TextUtils.equals(resultStatus, "8000")) {
                                MyToast.showTheToast(activity, "支付结果确认中");
                            } else {
                                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                MyToast.showTheToast(activity, "支付失败！");
                                activity.startActivity(new Intent(activity, MainMessage_1_.class));
                                activity.finish();
                            }
                        }
                        break;
                    }
                    case SDK_CHECK_FLAG: {
                        Toast.makeText(activity, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:
                        break;
                }
            }
        };
        succcessHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                Util.d("aliresult2", msg.toString());
                super.handleMessage(msg);
                progressDialog.dismiss();
                if (pay_type.equals("100")) {
                    MyToast.showTheToast(activity, msg.getData().getString("msg"));
                    activity.startActivity(new Intent(activity, MainMessage_1_.class));
                    activity.finish();
                    return;
                }
                String data = msg.getData().getString("data");
//                Util.d("032801", data);
                switch (pay_type) {
                    case "1":
                        try {
                            JSONTokener jsonTokener = new JSONTokener(data);
                            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                            //预授权支付宝
                            if (jsonObject.has("code_str") && !jsonObject.get("code_str").equals(JSONObject.NULL)) {
                                String code_str = jsonObject.getString("code_str");
                                pay(code_str);
                            } else {//非预授权支付宝
                                price = jsonObject.getString("order_fee");//价格
                                trade_sn = jsonObject.getString("order_num");
                                PARTNER = jsonObject.getString("partner");//APPID
                                SELLER = jsonObject.getString("seller");
                                RSA_PRIVATE = jsonObject.getString("app_private_key");
                                notify_url = jsonObject.getString("notify_url");//通知服务器的地址
                                name = jsonObject.getString("order_title");
                                pay();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "2":
                        try {
                            JSONTokener jsonTokener = new JSONTokener(data);
                            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                            String appid = jsonObject.getString("appid");
                            String partnerid = jsonObject.getString("partnerid");
                            String prepayid = jsonObject.getString("prepayid");
                            String package_ = jsonObject.getString("package");
                            String noncestr = jsonObject.getString("noncestr");
                            String timestamp = jsonObject.getString("timestamp");
                            String sign = jsonObject.getString("sign");
                            String order_num = jsonObject.getString("order_num");
                            String order_fee = jsonObject.getString("order_fee");
                            String order_title = jsonObject.getString("order_title");
                            trade_sn = order_num;
                            IWXAPI api = WXAPIFactory.createWXAPI(activity, appid);
                            try {
//                                if (jsonObject.has("data") && !(jsonObject.get("data").equals(JSONObject.NULL)))
                                if (!jsonObject.has("retmsg")) {
                                    PayReq req = new PayReq();
                                    req.appId = appid;
                                    req.partnerId = partnerid;
                                    req.prepayId = prepayid;
                                    req.packageValue = package_;
                                    req.nonceStr = noncestr;
                                    req.timeStamp = timestamp;
                                    req.sign = sign;
                                    req.extData = "app data"; // optional
//						Toast.makeText(getApplication(), "正常调起支付", Toast.LENGTH_SHORT).show();
                                    // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                                    api.sendReq(req);
                                } else {
                                    Log.d("PAY_GET", "返回错误" + jsonObject.getString("retmsg"));
                                    MyToast.showTheToast(activity, "返回错误" + jsonObject.getString("retmsg"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "3":
                        break;
                }
            }
        };
        errorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                Util.d("aliresult4", msg.toString());
                super.handleMessage(msg);
                String mst_str = msg.getData().getString("msg");
                MyToast.showTheToast(activity, mst_str);
                progressDialog.dismiss();
            }
        };
        succcessNoDataHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                Util.d("aliresult3", msg.toString());
                super.handleMessage(msg);
                String mst_str = msg.getData().getString("msg");
                MyToast.showTheToast(activity, mst_str);
                progressDialog.dismiss();
                activity.finish();
            }
        };
    }


    /**
     * 支付宝信息
     */
    /**
     * e
     * call alipay sdk pay. 调用SDK支付
     */
    private void pay() {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(activity).setTitle("警告").setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            activity.finish();
                        }
                    }).show();
            return;
        }
        String orderInfo = getOrderInfo(name, price, price);
//        Util.d("032803", orderInfo);
        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

//        final String payInfo = "alipay_sdk=alipay-sdk-php-20180705&app_id=2017112400137717&biz_content=%7B%22out_order_no%22%3A%22test201903271118262426%22%2C%22out_request_no%22%3A%22test201903271118262426%22%2C%22order_title%22%3A%221234567%22%2C%22amount%22%3A%220.01%22%2C%22product_code%22%3A%22PRE_AUTH_ONLINE%22%2C%22payee_user_id%22%3A%222088821648184781%22%2C%22extra_param%22%3A%22%7B%5C%22category%5C%22%3A%5C%22RENT_CAR_GOODS%5C%22%7D%22%2C%22enable_pay_channels%22%3A%22%5B%7B%5C%22payChannelType%5C%22%3A%5C%22PCREDIT_PAY%5C%22%7D%2C%7B%5C%22payChannelType%5C%22%3A%5C%22MONEY_FUND%5C%22%7D%2C%7B%5C%22payChannelType%5C%22%3A%5C%22CREDITZHIMA%5C%22%7D%5D%22%7D&charset=UTF-8&format=json&method=alipay.fund.auth.order.app.freeze&notify_url=%2FCeshi%2Fnotify.html&sign_type=RSA2&timestamp=2019-03-27+11%3A18%3A26&version=1.0&sign=hjQHp4GebUgUx8XHCSp3uL8nr%2BoVspZzaiAHEJlBJCysfK%2F9zjXQsgYrY65IaD06ru3FGy%2FKTGA6keQfKLOHIcmBbL5QELfL%2FsICUEbm%2FW4YovKcDZw9IyW2k5FwYnarxm2P4CRKo5DZTtY8ELJ1mZkPjVUeL%2FwxSLBalqKUsd5FS2FaGRdMbbMdfPTkUxhKkgNRj4VMq%2FtdKWqzaF3sRyyI4it2sv39M%2FrNEYocuItuaivYmMRhqhCc7EU%2FG0evjlinHoD%2BGI5h6Lirrf6bFSWM6ivBoYkwGK0cD8xSzzEloohTSKklxcgjjJ10p5GtcZ2FTTzGZ%2Bh%2FcXwoRqcBLQ%3D%3D";
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 支付宝信息
     */
    /**
     * e
     * call alipay sdk pay. 调用SDK支付
     */
    private void pay(String info) {
        final String payInfo = info;
//        Util.d("033004", payInfo);
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String subject, String body, String price) {
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";
        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";
        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + trade_sn + "\"";
        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";
        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";
        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";
        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + notify_url + "\"";
        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";
        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";
        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";
        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";
        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";
        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";
        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";
        return orderInfo;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    /**
     * http接口： Pay/initv1.html   获取订单信息
     */
    private void HttpGetOrderInfo_1() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("gid", gid));
//        Util.d("0327091", "uid: " + uid + ", ptype: " + pay_type + ", git: " + gid);
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv1.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_1(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_1(String response, String type) {
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
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

    private void HttpGetOrderInfo_2() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("order_num", gid));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv1.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_2(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_2(String response, String type) {
//        Util.d("info2", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }

    private void HttpGetOrderInfo_3() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("mjson", gid));
        dataList.add(new ParamTypeData("froms", "20"));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_3(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_3(String response, String type) {
//        Util.d("info3", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }

    private void HttpGetOrderInfo_4() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("mjson", gid));
        dataList.add(new ParamTypeData("froms", "30"));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_4(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_4(String response, String type) {
//        Util.d("info4", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }

    private void HttpGetOrderInfo_5() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("mjson", gid));
        dataList.add(new ParamTypeData("froms", "40"));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_5(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_5(String response, String type) {
//        Util.d("info5", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }

    private void HttpGetOrderInfo_6() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("order_num", gid));
        dataList.add(new ParamTypeData("froms", "10"));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_6(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_6(String response, String type) {
//        Util.d("info6", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }

    private void HttpGetOrderInfo_7() {
        List<ParamTypeData> dataList = new ArrayList<>();
        dataList.add(new ParamTypeData("uid", uid));
        dataList.add(new ParamTypeData("ptype", pay_type));
        dataList.add(new ParamTypeData("gid", gid));
        dataList.add(new ParamTypeData("froms", "50"));
        new OkHttpConnect(activity, PubFunction.api + "Pay/initv2.html", dataList, HeaderTypeData.HEADER_Whit_APTK_APUD_PRO(activity, uid), new OkHttpConnect.ResultListener() {
            @Override
            public void onSuccessResult(String response, String type) {
                onDataHttpGetOrderInfo_7(response, type);
                progressDialog.dismiss();
            }
        }).startHttpThread();
    }

    private void onDataHttpGetOrderInfo_7(String response, String type) {
//        Util.d("info7", response);
        if (type.equals("0")) {
            MyToast.showTheToast(activity, response);
            sendMessage(errorHandler, response);
        } else {
            try {
                JSONObject jsonObject_response = new JSONObject(response);
                String msg = jsonObject_response.getString("msg");
                String status = jsonObject_response.getString("status");
                System.out.println(jsonObject_response);
                if (status.equals("1")) {
                    if (jsonObject_response.has("data")) {
                        String data = jsonObject_response.getString("data");
                        sendMessage(succcessHandler, msg, data);
                    } else {
                        sendMessage(succcessNoDataHandler, msg, "");
                    }
                } else {
                    sendMessage(errorHandler, msg);
                }
            } catch (Exception e) {
                sendMessage(errorHandler, "JSON：" + e.toString());
            }
        }
    }
}





