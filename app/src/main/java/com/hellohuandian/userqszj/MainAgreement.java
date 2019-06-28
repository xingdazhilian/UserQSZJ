package com.hellohuandian.userqszj;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.hellohuandian.userqszj.pub.PubFunction;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_agreement)
public class MainAgreement extends BaseActivity {

    @ViewById
    LinearLayout page_return;
    @ViewById
    WebView web_view;

    @AfterViews
    void afterViews() {
        if (PubFunction.isConnect(activity)) {
            web_view.setInitialScale(200);
            WebSettings webSettings = web_view.getSettings();
            webSettings.setJavaScriptEnabled(true);
            web_view.loadUrl("http://img01.mtuke.com/upload/agreement.html");

        }
    }

    @Click
    void page_return() {
        this.finish();
    }

}
