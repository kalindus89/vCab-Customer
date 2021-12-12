package com.vcab.vcabcustomer.model.events_notification;

public class AcceptRequestFromDriver {

    String tripId;
    String driverUid;

    public AcceptRequestFromDriver(String tripId, String driverUid) {
        this.tripId = tripId;
        this.driverUid = driverUid;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }
}
