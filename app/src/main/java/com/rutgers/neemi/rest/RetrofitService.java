package com.rutgers.neemi.rest;

import com.rutgers.neemi.model.InstagramResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by suitcase on 3/15/18.
 */

public interface RetrofitService {

    @GET("v1/tags/{tag_name}/media/recent")
    Call<InstagramResponse> getTagPhotos(@Path("tag_name") String tag_name,
                                         @Query("access_token") String access_token);


    @GET("v1/users/self/media/recent")
    Call<InstagramResponse> getRecentMedia(@Query("access_token") String access_token);

    @GET("v1/users/self/media/recent")
    Call<InstagramResponse> getRecentMediaAfterID(@Query("access_token") String access_token, @Query("MIN_ID") String id);

}
