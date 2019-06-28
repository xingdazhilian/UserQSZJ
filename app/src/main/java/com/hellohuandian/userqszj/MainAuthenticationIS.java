package com.hellohuandian.userqszj;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by hasee on 2017/6/6.
 */
@EActivity(R.layout.main_authentication_is)
public class MainAuthenticationIS extends BaseActivity {
    @ViewById
    LinearLayout page_return;
    @ViewById
    TextView user_name, user_id;
    @ViewById
    ImageView user_card_1_img, user_card_2_img;

    @AfterViews
    void afterViews() {
        String user_id_str = getIntent().getStringExtra("id_card");
        String real_name_str = getIntent().getStringExtra("real_name");
        String front_img_str = getIntent().getStringExtra("front_img");
        String reverse_img_str = getIntent().getStringExtra("reverse_img");
        user_name.setText(real_name_str);
        user_id.setText(user_id_str);
        Picasso.with(activity).load(front_img_str).rotate(270f).into(user_card_1_img);
        Picasso.with(activity).load(reverse_img_str).rotate(270f).into(user_card_2_img);
    }

    @Click
    void page_return() {
        this.finish();
    }
}




