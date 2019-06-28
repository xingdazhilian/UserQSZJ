package com.hellohuandian.userqszj;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.hellohuandian.userqszj.pub.ProgressDialog;

public class BaseFragment extends Fragment {
    protected ProgressDialog progressDialog;
    protected SharedPreferences sharedPreferences;
    protected String uid;

    protected void initParam() {
        sharedPreferences = getActivity().getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        uid = sharedPreferences.getString("id", "");
        progressDialog = new ProgressDialog(getActivity());
    }
}
