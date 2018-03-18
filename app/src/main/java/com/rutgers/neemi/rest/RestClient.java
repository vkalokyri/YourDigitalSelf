package com.rutgers.neemi.rest;

import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.rutgers.neemi.parser.InitiateScript.config;

/**
 * Created by suitcase on 3/15/18.
 */

public class RestClient {

    public static RetrofitService getRetrofitService() {
        return new Retrofit.Builder()
                .baseUrl(ConfigReader.getInstance().getStr(PROPERTIES.INSTAGRAM_BASE_URL))
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitService.class);
    }
}
