package com.vcab.vcabcustomer;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.snackbar.Snackbar;
import com.vcab.vcabcustomer.model.DriverGeoModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Messages_Common_Class {


    public static Set<DriverGeoModel> driverFound = new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> markerList = new HashMap<>();

    public static void showToastMsg(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(String msg, View view) {

        Snackbar.make(view,msg,Snackbar.LENGTH_SHORT).show();
    }
}
