package com.hellohuandian.userqszj.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hellohuandian.userqszj.R;

public class MyScrollView extends ScrollView {
    private MyScrollView myScrollView;
    private Context context;
    private DisplayMetrics dm;
    private Point size;
    private int contentHeight;//this的高度
    private int type = 0;

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
        init();
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void init() {
        size = new Point();
        dm = new DisplayMetrics();
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
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(dm);
        display.getSize(size);
        //this的高度=屏幕高度-状态栏高度-title的45dp高度（title就是骑士之家那个标题栏）
        contentHeight = (int) (size.y - statusBarHeight1 - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, dm));

        FrameLayout view_1 = findViewById(R.id.panel_1);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view_1.getLayoutParams();
        params.height = (int) (contentHeight - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, dm));
        view_1.setLayoutParams(params);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int y = (int) ev.getRawY();
        LinearLayout view = (LinearLayout) this.getChildAt(0);
        FrameLayout view_1 = (FrameLayout) view.getChildAt(0);
        int[] location = new int[2];
        view_1.getLocationOnScreen(location);
        int view_1_bottom = location[1];
        if (y < view_1_bottom + (contentHeight * 2 / 3)) {
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
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    A_1_open();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            objectAnimator.start();
            type = 0;
//            Main.cleanLineHandler.sendMessage(new Message());
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
//            Main.cleanLineHandler.sendMessage(new Message());
        }
    }

}
