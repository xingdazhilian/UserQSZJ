package com.hellohuandian.userqszj.http;

import android.app.Activity;

import com.hellohuandian.userqszj.pub.PubFunction;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class HttpConnect {

    private Activity activity;
    private String path;
    private List<NameValuePair> paramList = null;
    private String[][] headerArray;
    private String result_str = "";
    private String result_type = "";
    private ResultListener listener;
    private Thread httpThread = new Thread() {

        @Override
        public void run() {
            super.run();

            if (!PubFunction.isConnect(activity)) {
                result_str = "无网络链接，请检查您的网络设置！";
                result_type = "0";
            } else {
                HttpPost httpPost = new HttpPost(path);
                httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
                httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

                if (headerArray.length > 0) {
                    for (int i = 0; i < headerArray.length; i++) {
                        final String[] arrays = headerArray[i];
                        Header header1 = new Header() {
                            @Override
                            public String getName() {
                                return arrays[0];
                            }

                            @Override
                            public String getValue() {
                                return arrays[1];
                            }

                            @Override
                            public HeaderElement[] getElements() throws ParseException {
                                return new HeaderElement[0];
                            }
                        };
                        httpPost.addHeader(header1);
                    }
                }

                try {
                    HttpEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                    httpPost.setEntity(entity);
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse httpResponse = client.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        result_str = EntityUtils.toString(httpResponse.getEntity());
                        result_type = "1";

                    } else {
                        result_str = "Error：" + httpResponse.getStatusLine().getStatusCode();
                        result_type = "0";
                    }
                } catch (Exception e) {
                    result_str = "Error：" + e;
                    result_type = "0";
                }
            }
            listener.onSuccessResult(result_str, result_type);
        }
    };


    public HttpConnect(Activity activity, String path, List<NameValuePair> paramList, String[][] headerArray, ResultListener listener) {
        this.activity = activity;
        this.path = path;
        this.paramList = paramList;
        this.headerArray = headerArray;
        this.listener = listener;
    }

    ;

    public HttpConnect(Activity activity, String path, List<NameValuePair> paramList, ResultListener listener) {
        this.activity = activity;
        this.path = path;
        this.paramList = paramList;
        this.listener = listener;
    }

    ;

    public void startHttpThread() {
        httpThread.start();
    }

    public interface ResultListener {
        public void onSuccessResult(String response, String type);
    }


}
