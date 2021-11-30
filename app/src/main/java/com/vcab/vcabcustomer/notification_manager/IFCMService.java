package com.vcab.vcabcustomer.notification_manager;


import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAXryvZCQ:APA91bF4LmHo7Z8wWmIl88E7Tzt5ofnhcn-_eOtWlDI1ojd3NgkYSTuY-cTbUyNYAjfwvS1V5SVWr-8jSNEyMdI3ieF3lVs8cCNhZwj9g6RGOjTTNQmsGht7ZPr9CzRkC2QuYwVMKHKH"
                    // Your server key from firebase project settings, cloud messaging
            }
    )

    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body); //make sure Observable is io.reactivex.Observable;
}

