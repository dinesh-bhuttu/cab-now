package com.example.ridercabnow.models;

public class Profile {
    private String rider_name;
    private String rider_email;
    private String rider_phone;
    private String type;
    private float average_Rating;


    public Profile() { }

    public Profile(String rider_name, String rider_email, String rider_phone, String type, float average_Rating) {
        this.rider_name = rider_name;
        this.rider_email = rider_email;
        this.rider_phone = rider_phone;
        this.type = type;
        this.average_Rating = average_Rating;
    }

    public String getRider_name() {
        return rider_name;
    }

    public void setRider_name(String rider_name) {
        this.rider_name = rider_name;
    }

    public String getRider_email() {
        return rider_email;
    }

    public void setRider_email(String rider_email) {
        this.rider_email = rider_email;
    }

    public String getRider_phone() {
        return rider_phone;
    }

    public void setRider_phone(String rider_phone) {
        this.rider_phone = rider_phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getAverage_Rating() {
        return average_Rating;
    }

    public void setAverage_Rating(float average_Rating) {
        this.average_Rating = average_Rating;
    }
}
