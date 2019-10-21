package com.example.ridercabnow.models;

public class History {

    private Latlng destination;
    private String payment;
    private String price;
    private String rating_to_driver;

    public History() {}

    public History(Latlng destination, String payment, String price, String rating_to_driver) {
        this.destination = destination;
        this.payment = payment;
        this.price = price;
        this.rating_to_driver = rating_to_driver;
    }

    public Latlng getDestination() {
        return destination;
    }

    public void setDestination(Latlng destination) {
        this.destination = destination;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRating_to_driver() {
        return rating_to_driver;
    }

    public void setRating_to_driver(String rating_to_driver) {
        this.rating_to_driver = rating_to_driver;
    }
}
