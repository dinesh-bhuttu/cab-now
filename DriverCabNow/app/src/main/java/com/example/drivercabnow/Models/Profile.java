package com.example.drivercabnow.Models;

public class Profile {
    private String driver_name;
    private String driver_email;
    private String driver_phone;
    private String type;
    private float average_Rating;
    private String upiid;


    public Profile() { }

    public Profile(String driver_name, String driver_email, String driver_phone, String type, float average_Rating, String upiid) {
        this.driver_name = driver_name;
        this.driver_email = driver_email;
        this.driver_phone = driver_phone;
        this.type = type;
        this.upiid = upiid;
        this.average_Rating = average_Rating;
    }

    public String getDriver_name() {
        return driver_name;
    }
    public String getUpiid(){
        return upiid;
    }

    public void setUpiid(){
        this.upiid = upiid;
    }
    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_email() {
        return driver_email;
    }

    public void setDriver_email(String driver_email) {
        this.driver_email = driver_email;
    }

    public String getDriver_phone() {
        return driver_phone;
    }

    public void setDriver_phone(String driver_phone) {
        this.driver_phone = driver_phone;
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
