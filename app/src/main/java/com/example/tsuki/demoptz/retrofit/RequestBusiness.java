package com.example.tsuki.demoptz.retrofit;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Android Studio
 * Author: tsuki
 * Time: 04/10/2017
 */

public class RequestBusiness {
    public static Map<String, String> login(String username, String device_id, String password, String agent) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("device_type", "mobile");
        params.put("device_id", device_id);
        params.put("password", password);
        params.put("action", "login");
        params.put("agent", agent);

        Log.d("eee", "param login: " + params);
        return params;
    }

    public static Map<String, String> queryDetail(String token) {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("version", "004");

        Log.d("eee", "param queryDetail: " + params);
        return params;
    }

    public static Map<String, String> fieldDetail(String camera_id, String site_id) {
        Map<String, String> params = new HashMap<>();
        params.put("camera_id", camera_id);
        params.put("site_id", "site_id");

        Log.d("eee", "param fieldDetail: " + params);
        return params;
    }

}
