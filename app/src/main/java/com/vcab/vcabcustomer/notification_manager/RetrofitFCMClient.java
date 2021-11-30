package com.vcab.vcabcustomer.notification_manager;

import com.vcab.vcabcustomer.retrofit_remote.Credentials;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitFCMClient {

    public static Retrofit retrofitInstance;

    public static Retrofit getInstance() {

        return retrofitInstance == null ? new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : retrofitInstance;//make it fast
    }
}
