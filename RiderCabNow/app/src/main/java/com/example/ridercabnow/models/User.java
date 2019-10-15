package com.example.ridercabnow.models;

// FIREBASE DATABASE STRUCTURE
//        "firebase_gen_rider_id":{
//        "Rider_email":email,
//        "Rider_id":phone_number,
//        "RiderName":"abc",
//        "Rating":0,
//        "Ride_history":[rid1, rid2, rid3...],
//        },
public class User {
    private String Rider_email, Rider_name, Rider_phone;
    private Double Average_Rating;
    private String type="RIDER";
    private Ride rides;


    public User(){}

    public User(String Rider_email, String Rider_name, String Rider_phone) {
        this.Rider_email = Rider_email;
        this.Rider_name = Rider_name;
        this.Rider_phone = Rider_phone;
        this.Average_Rating = 0.0;
    }

    // GETTERS
    public String getRider_email() {
        return Rider_email;
    }
    public String getRider_name() {
        return Rider_name;
    }
    public String getRider_phone() {
        return Rider_phone;
    }
    public String getType() {
        return type;
    }
    public Double getAverage_Rating() {
        return Average_Rating;
    }

    // SETTERS
    public void setAverage_Rating(Double average_Rating) {
        Average_Rating = average_Rating;
    }
    public void setRider_email(String rider_email) {
        Rider_email = rider_email;
    }
    public void setRiderName(String rider_name) {
        Rider_name = rider_name;
    }
    public void setRider_id(String rider_phone) {
        Rider_phone = rider_phone;
    }
}
