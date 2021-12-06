package com.vcab.vcabcustomer.model;

public class TripPlanModel {
    private String customerUid, driverUid;
    private DriverInfoModel driverInfoModel;
    private CustomerModel customerModel;
    private String originCustomer;
    private String destinationCustomer;
    private String distanceCustomerPickup, distanceCustomerDestination;
    private String durationCustomerPickup, durationDestination;
    private String ticketNumber;
    private double currentLat, currentLng;
    private boolean isDone, isCancel;


    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }



    public TripPlanModel() {
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void setCustomerUid(String customerUid) {
        this.customerUid = customerUid;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    public DriverInfoModel getDriverInfoModel() {
        return driverInfoModel;
    }

    public void setDriverInfoModel(DriverInfoModel driverInfoModel) {
        this.driverInfoModel = driverInfoModel;
    }

    public CustomerModel getCustomerModel() {
        return customerModel;
    }

    public void setCustomerModel(CustomerModel customerModel) {
        this.customerModel = customerModel;
    }

    public String getOriginCustomer() {
        return originCustomer;
    }

    public void setOriginCustomer(String originCustomer) {
        this.originCustomer = originCustomer;
    }

    public String getDestinationCustomer() {
        return destinationCustomer;
    }

    public void setDestinationCustomer(String destinationCustomer) {
        this.destinationCustomer = destinationCustomer;
    }

    public String getDistanceCustomerPickup() {
        return distanceCustomerPickup;
    }

    public void setDistanceCustomerPickup(String distanceCustomerPickup) {
        this.distanceCustomerPickup = distanceCustomerPickup;
    }

    public String getDistanceCustomerDestination() {
        return distanceCustomerDestination;
    }

    public void setDistanceCustomerDestination(String distanceCustomerDestination) {
        this.distanceCustomerDestination = distanceCustomerDestination;
    }

    public String getDurationCustomerPickup() {
        return durationCustomerPickup;
    }

    public void setDurationCustomerPickup(String durationCustomerPickup) {
        this.durationCustomerPickup = durationCustomerPickup;
    }

    public String getDurationDestination() {
        return durationDestination;
    }

    public void setDurationDestination(String durationDestination) {
        this.durationDestination = durationDestination;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}
