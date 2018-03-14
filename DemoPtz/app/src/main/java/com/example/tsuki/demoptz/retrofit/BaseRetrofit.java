package com.example.tsuki.demoptz.retrofit;

import com.example.tsuki.demoptz.utils.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Android Studio
 * Author: tsuki
 * Time: 06/09/2017
 */

class BaseRetrofit {
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(Constant.URL + "/")
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();


    static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
