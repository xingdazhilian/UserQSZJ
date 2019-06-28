package com.hellohuandian.userqszj.pub.scanCode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellohuandian.userqszj.R;
import com.hellohuandian.userqszj.pub.MyToast;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.scancode_activity)
public class ScanCodeActivity extends FragmentActivity {

    @ViewById
    ImageView light;
    @ViewById
    LinearLayout capture_imageview_back;
    @ViewById
    EditText input_code;
    @ViewById
    TextView submit_code;


    int light_type = 0;

    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
//            Util.d("032701", result);
            Intent intent = getIntent();
            intent.putExtra("codedContent", result);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent intent = getIntent();
            intent.putExtra("codedContent", "");
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @AfterViews
    void afterViews() {
        CaptureFragment captureFragment = new CaptureFragment();
//        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.my_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        /**
         * 替换我们的扫描控件
         */
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();
    }

    @Click
    void capture_imageview_back() {
        this.finish();
    }

    @Click
    void light() {
        if (light_type == 0) {
            light_type = 1;
            CodeUtils.isLightEnable(true);
        } else if (light_type == 1) {
            light_type = 0;
            CodeUtils.isLightEnable(false);
        }
    }

    @Click
    void submit_code() {
        String str = input_code.getText().toString().trim();
        if (str.equals("")) {
            MyToast.showTheToast(this, "输入信息不能为空！");
        } else {
            Intent intent = getIntent();
            intent.putExtra("codedContent", str);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
