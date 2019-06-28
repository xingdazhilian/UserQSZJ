package com.hellohuandian.userqszj.extend_plug.ViewPagerScroller;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hellohuandian.userqszj.R;

public class MyScrollView extends ScrollView {

    private Context context;
    private int tHeight;
    private int type = 0;

    private MyScrollView myScrollView;
    private int height_1, height_2;


    public MyScrollView(Context context) {
        this(context, null);
        this.context = context;
    }


    public MyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setChildViewHeight(int height_1, int height_2) {
        this.height_1 = height_1;
        this.height_2 = height_2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        myScrollView = this;

        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        tHeight = wm.getDefaultDisplay().getHeight() - statusBarHeight1 - dip2px(context, 0) - dip2px(context, 45);


        LinearLayout view = (LinearLayout) this.getChildAt(0);
        LinearLayout view_1 = (LinearLayout) view.getChildAt(0);
        LinearLayout view_2 = (LinearLayout) view.getChildAt(1);

        view_1 = (LinearLayout) this.findViewById(R.id.panel_1);
        LinearLayout.LayoutParams linearParams_1 = (LinearLayout.LayoutParams) view_1.getLayoutParams(); //取控件textView当前的布局参数 linearParams.height = 20;// 控件的高强制设成20
        linearParams_1.height = tHeight - dip2px(context, 240);
        view_1.setLayoutParams(linearParams_1);

        view_2 = (LinearLayout) this.findViewById(R.id.panel_2);
        LinearLayout.LayoutParams linearParams_2 = (LinearLayout.LayoutParams) view_2.getLayoutParams(); //取控件textView当前的布局参数 linearParams.height = 20;// 控件的高强制设成20
        linearParams_2.height = dip2px(context, 240);
        view_2.setLayoutParams(linearParams_2);


//        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", 0, dip2px(context, 240) );
//        objectAnimator.setDuration(1);
//
//        objectAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//
//        objectAnimator.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();

        LinearLayout view = (LinearLayout) this.getChildAt(0);
        LinearLayout view_1 = (LinearLayout) view.getChildAt(0);
        LinearLayout view_2 = (LinearLayout) view.getChildAt(1);

        int[] location = new int[2];
        view_1.getLocationOnScreen(location);
        int view_1_bottom = location[1];

        if (y < view_1_bottom + (tHeight / 3 * 2)) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    public void A_1() {

        if (type == 1) {
            myScrollView.post(new Runnable() {
                @Override
                public void run() {
                    myScrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", 0, dip2px(context, 240));
            objectAnimator.setDuration(240);

            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    A_1_open();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            objectAnimator.start();
            type = 0;
        } else {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", dip2px(context, 240), 0);
            objectAnimator.setDuration(240);

            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    myScrollView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            objectAnimator.start();
            type = 1;
        }


    }

    public void A_1_close() {

        if (type == 1) {

            type = 0;

            myScrollView.post(new Runnable() {
                @Override
                public void run() {
                    myScrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", 0, dip2px(context, 240));
            objectAnimator.setDuration(240);

            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    myScrollView.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            objectAnimator.start();
        }
    }

    public void A_1_open() {

        type = 1;

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationY", dip2px(context, 240), 0);
        objectAnimator.setDuration(240);

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

                myScrollView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        objectAnimator.start();

    }


}
