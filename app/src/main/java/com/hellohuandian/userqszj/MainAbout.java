package com.hellohuandian.userqszj;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.hellohuandian.userqszj.pub.PubFunction;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_about)
public class MainAbout extends BaseActivity {

    @ViewById
    LinearLayout page_return;
    @ViewById
    WebView mWebView;

    @AfterViews
    void afterViews() {
        if (PubFunction.isConnect(activity)) {
            mWebView.setInitialScale(75);
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewController());
            mWebView.loadUrl("https://app.halouhuandian.com/introduction/index");
        }
    }

    @Click
    void page_return() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public class WebViewController extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
