package com.vcab.vcabcustomer.call_back_interfaces;

import com.vcab.vcabcustomer.model.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
