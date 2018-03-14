package com.example.tsuki.demoptz.retrofit;

import com.example.tsuki.demoptz.model.LoginData;
import com.example.tsuki.demoptz.utils.Constant;
import com.google.gson.JsonElement;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by Tsuki
 * on 6/5/2017.
 */

public interface APIService {
    @GET(Constant.LOGIN_API)
    Call<LoginData> login(@QueryMap Map<String, String> map);

    @FormUrlEncoded
    @POST(Constant.DETAIL_CAMERA)
    Call<JsonElement> detailCamera(@QueryMap Map<String, String> map, @FieldMap Map<String, String> mapfield);



}
