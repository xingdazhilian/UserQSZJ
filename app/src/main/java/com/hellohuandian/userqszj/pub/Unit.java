package com.hellohuandian.userqszj.pub;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Created by apple on 2017/12/2.
 */

public class Unit {


    public static Bitmap generateBitmap(String str, int width, int height) {

        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, width, height);
            matrix = deleteWhite(matrix);//删除白边
            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }


    public static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return Integer.toHexString(CRC);
    }

    public static String getCRC(int[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= (bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return Integer.toHexString(CRC);
    }


    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(hexString
                    .substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static char backchar(int backnum) {
        char strChar = (char) backnum;
        return strChar;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int[] percentageClass(int[] b) {
        int a[] = new int[10];

        for (int i = 0; i < a.length; i++) {
            a[i] = 0;
        }

        for (int i = 0; i < b.length; i++) {
            int c = b[i];
            if (c > 0 && c <= 10) {
                a[9] = a[9] + 1;
            } else if (c > 10 && c <= 20) {
                a[8] = a[8] + 1;
            } else if (c > 20 && c <= 30) {
                a[7] = a[7] + 1;
            } else if (c > 30 && c <= 40) {
                a[6] = a[6] + 1;
            } else if (c > 40 && c <= 50) {
                a[5] = a[5] + 1;
            } else if (c > 50 && c <= 60) {
                a[4] = a[4] + 1;
            } else if (c > 60 && c <= 70) {
                a[3] = a[3] + 1;
            } else if (c > 70 && c <= 80) {
                a[2] = a[2] + 1;
            } else if (c > 80 && c <= 90) {
                a[1] = a[1] + 1;
            } else if (c > 90 && c <= 100) {
                a[0] = a[0] + 1;
            }
        }

        return a;
    }

    public static int renturnType(int c) {
        int a = 0;

        if (c >= 0 && c <= 10) {
            a = 9;
        } else if (c > 10 && c <= 20) {
            a = 8;
        } else if (c > 20 && c <= 30) {
            a = 7;
        } else if (c > 30 && c <= 40) {
            a = 6;
        } else if (c > 40 && c <= 50) {
            a = 5;
        } else if (c > 50 && c <= 60) {
            a = 4;
        } else if (c > 60 && c <= 70) {
            a = 3;
        } else if (c > 70 && c <= 80) {
            a = 2;
        } else if (c > 80 && c <= 90) {
            a = 1;
        } else if (c > 90 && c <= 100) {
            a = 0;
        }
        return a;
    }

    public static int getMaxIndex(int[] A, int nowDoor) {
        int j = 0;
        int max = A[0];
        for (int i = 0; i < A.length; i++) {

            if (i != nowDoor - 1) {
                if (A[i] > max) { // 判断最大值
                    max = A[i];
                    j = i;
                }
            }


        }

        return j;
    }


    public static int getMaxIndex(int[] A, int nowDoor, int exDoor) {
        int j = 0;
        int max = A[0];
        for (int i = 0; i < A.length; i++) {

            if (i != nowDoor - 1 && i != exDoor - 1) {
                if (A[i] > max) { // 判断最大值
                    max = A[i];
                    j = i;
                }
            }


        }

        return j;
    }

    public static int getCount(String[] A) {
        int j = 0;
        for (int i = 0; i < A.length; i++) {

            if (!A[i].equals("0000000000000000") && !A[i].equals("FFFFFFFFFFFFFFFF")) {

                j = j + 1;

            }


        }

        return j;
    }

    public static void hideBottomUIMenu(Activity activity) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


}
