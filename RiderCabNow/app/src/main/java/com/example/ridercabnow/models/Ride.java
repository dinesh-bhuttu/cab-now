package com.example.ridercabnow.models;

// Object Template
//        "rides" : {
        //    "rid" : {
        //        "source" : { "lat" : "latVal", "lng" : "lngVal" },
        //        "destination": { "lat" : "latVal", "lng" : "lngVal" },
        //        "status" : "finished or cancelled or active or looking",
        //        "price" : "value",
        //        "distance" : "x kms",
        //        "payment" : "cash or upi",
        //        "driver": "driverUID",
        //        "rider" : "riderUID",
        //        "vehicle" : "auto or micro or sedan"
//            }
//        }
public class Ride {

    // Latlng is our model class, not the builtin LatLng class
    private Latlng source;
    private Latlng destination;
    private String status;
    private String price;
    private String distance;
    private String payment;
    private String driver;
    private String rider;
    private String vehicle;

    public Ride() {}

    public Ride(Latlng source, Latlng destination, String status, String price, String distance, String payment, String driver, String rider, String vehicle) {
        this.source = source;
        this.destination = destination;
        this.status = status;
        this.price = price;
        this.distance = distance;
        this.payment = payment;
        this.driver = driver;
        this.rider = rider;
        this.vehicle = vehicle;
    }

    public Latlng getSource() {
        return source;
    }

    public void setSource(Latlng source) {
        this.source = source;
    }

    public Latlng getDestination() {
        return destination;
    }

    public void setDestination(Latlng destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }
}
