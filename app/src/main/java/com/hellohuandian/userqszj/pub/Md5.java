package com.hellohuandian.userqszj.pub;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

/**
 * Created by hasee on 2017/6/8.
 */
public class Md5 {

    final static String token_key = "K7JEUN6BH116M93A32A7H8R8BD9I505B";

    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public String getDateToken() {

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new java.util.Date());
        String dateMd5 = stringToMD5(date);

        String all = dateMd5 + token_key;
        String allMd5 = stringToMD5(all);

        return allMd5;
    }

    public String getUidToken(String Uid) {

        String dateMd5 = stringToMD5(Uid);
        String all = dateMd5 + token_key;
        String allMd5 = stringToMD5(all);
        return allMd5;
    }
}
