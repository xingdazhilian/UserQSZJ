package com.hellohuandian.userqszj.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellohuandian.userqszj.R;

/**
 * 描述：拨打电话的弹窗碎片，被多个页面使用
 * modified by 田彦宇 20190418
 */
public class AutenticationFragment extends DialogFragment implements View.OnClickListener {

    private OnDialogItemClickListener listener;

    public static AutenticationFragment newInstance() {
        return new AutenticationFragment();
    }

    @Nullable
    @Override //通过重写Fragment的onCreateView()实现dialog的UI
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //1、通过inflate，根据layout XML定义，创建view
        View view = inflater.inflate(R.layout.alertdialog_select_image_2, container, false);
        TextView left = view.findViewById(R.id.left);
        TextView right = view.findViewById(R.id.right);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            switch (v.getId()) {
                case R.id.left:
                    listener.onLeftClick();
                    dismiss();
                    break;
                case R.id.right:
                    listener.onRightClick();
                    dismiss();
                    break;
            }
        }
    }

    public void setOnDialogItemClickListener(OnDialogItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDialogItemClickListener {
        void onLeftClick();

        void onRightClick();
    }

}
