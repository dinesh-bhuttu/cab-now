package com.example.drivercabnow.Models;

public class HistoryItem {

    private float price;
    private Latlng destination;
    private String driverName;
    private String payment;
    private float rating_to_rider;
    private String vehicle;

    public HistoryItem() { }

    public HistoryItem(float price, Latlng destination, String driverName, String payment, float rating_to_rider, String vehicle) {
        this.price = price;
        this.destination = destination;
        this.driverName = driverName;
        this.payment = payment;
        this.rating_to_rider = rating_to_rider;
        this.vehicle = vehicle;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Latlng getDestination() {
        return destination;
    }

    public void setDestination(Latlng destination) {
        this.destination = destination;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public float getRating_to_rider() {
        return rating_to_rider;
    }

    public void setRating_to_rider(float rating_to_rider) {
        this.rating_to_rider = rating_to_rider;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
