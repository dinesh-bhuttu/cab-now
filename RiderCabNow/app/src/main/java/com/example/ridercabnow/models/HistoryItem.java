package com.example.ridercabnow.models;

public class HistoryItem {

    private float price;
    private Latlng destination;
    private String driverName;
    private String payment;
    private float rating_to_driver;
    private String vehicle;

    public HistoryItem() { }

    public HistoryItem(float price, Latlng destination, String driverName, String payment, float rating_to_driver, String vehicle) {
        this.price = price;
        this.destination = destination;
        this.driverName = driverName;
        this.payment = payment;
        this.rating_to_driver = rating_to_driver;
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

    public float getRating_to_driver() {
        return rating_to_driver;
    }

    public void setRating_to_driver(float rating_to_driver) {
        this.rating_to_driver = rating_to_driver;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
