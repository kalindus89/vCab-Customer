package com.vcab.vcabcustomer.retrofit_remote;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGoogleApiInterface {

//https://developers.google.com/maps/documentation/directions/get-directions

    //you must enable billing for your api and enable direction in Google cloud platform

    @GET("maps/api/directions/json")
    Observable<String > getDirection(
            @Query("mode") String mode,
            @Query("transit_routing_preference") String transit_routing_preference,
            @Query("origin") String from,
            @Query("destination") String to,
            @Query("key") String key
    );
}
