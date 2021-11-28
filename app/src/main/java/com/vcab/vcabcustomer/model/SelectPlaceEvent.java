package com.vcab.vcabcustomer.model;

import com.google.android.gms.maps.model.LatLng;

public class SelectPlaceEvent {

    private LatLng userOrigin,userDestination;

    public SelectPlaceEvent(LatLng userOrigin, LatLng userDestination) {
        this.userOrigin = userOrigin;
        this.userDestination = userDestination;
    }

    public LatLng getUserOrigin() {
        return userOrigin;
    }

    public void setUserOrigin(LatLng userOrigin) {
        this.userOrigin = userOrigin;
    }

    public LatLng getUserDestination() {
        return userDestination;
    }

    public void setUserDestination(LatLng userDestination) {
        this.userDestination = userDestination;
    }
}
