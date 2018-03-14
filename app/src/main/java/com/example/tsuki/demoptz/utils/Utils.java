package com.example.tsuki.demoptz.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by root
 * on 24/06/2016.
 */

public class Utils {
    public static void mLogE(String message) {
        Log.e("eee", message);
    }

    public static void mLogD(String message) {
        Log.d("eee", message);
    }

    //Log e with large string
    public static void mLargeLogE(String message) {
        if (message.length() > 4000) {
            Log.e("eee", "---------" + message.substring(0, 4000));
            mLargeLogE(message.substring(4000));
        } else {
            Log.e("eee", "---------" + message);
        }
    }

    public static String encypt(String text) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = new byte[0];
            try {
                hash = digest.digest(text.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format("%0" + (hash.length * 2) + 'x', new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


}
