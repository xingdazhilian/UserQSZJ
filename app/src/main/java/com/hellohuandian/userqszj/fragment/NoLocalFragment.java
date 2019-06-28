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

public class NoLocalFragment extends DialogFragment implements View.OnClickListener {
    private OnDialogItemClickListener listener;

    public static NoLocalFragment getInstance() {
        return new NoLocalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alertdialog_no_local, container, false);
        TextView success = view.findViewById(R.id.payAlertdialogSuccess);
        TextView error = view.findViewById(R.id.payAlertdialogError);
        success.setOnClickListener(this);
        error.setOnClickListener(this);
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
                case R.id.payAlertdialogSuccess:
                    listener.onSuccessClick();
                    dismiss();
                    break;
                case R.id.payAlertdialogError:
                    listener.onErrorClick();
                    dismiss();
                    break;
            }
        }
    }

    public void setOnDialogItemClickListener(OnDialogItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDialogItemClickListener {
        void onSuccessClick();

        void onErrorClick();
    }
}
