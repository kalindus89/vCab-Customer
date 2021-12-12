package com.vcab.vcabcustomer.model.events_notification;

public class DriverCompletedTripEvent {

    private String driverUid;

    public DriverCompletedTripEvent(String driverUid) {
        this.driverUid=driverUid;

    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }
}
