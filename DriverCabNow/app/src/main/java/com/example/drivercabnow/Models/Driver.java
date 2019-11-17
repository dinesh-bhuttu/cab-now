package com.example.drivercabnow.Models;
// FIREBASE DATABASE STRUCTURE
//{
//        "firebase_gen_driver_id":{
//        "Driver_id":phone_number,
//        "Driver_name":"abcd",
//        "Cab_type": "Micro" ["Micro", "Mini", "LUX"],
//        "Cab_status":"Available" ["Available", "Transit","NA","Busy"],
//        "Vehicle_no":"",
//        "License_no":"",
//        "Average_rating":4,
//        "Current_Location":["lat","lng"],
//        "UPI_Id":"abc@abc"
//        }
//}


public class Driver {
    private String Driver_email, Driver_name, Driver_phone, UPI_Id, Vehicle_no, License_no, Cab_type, Cab_status;
    private Double Average_rating;
    private String Type="DRIVER";
    private Latlng source;

    public Driver(){}

    public void setDriver_email(String driver_email) {
        Driver_email = driver_email;
    }

    public void setDriver_name(String driver_name) {
        Driver_name = driver_name;
    }

    public void setDriver_phone(String driver_phone) {
        Driver_phone = driver_phone;
    }

    public void setUPI_Id(String UPI_Id) {
        this.UPI_Id = UPI_Id;
    }

    public String getLicense_no() {
        return License_no;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public Driver(String Driver_email, String Driver_name, String Driver_phone, String Vehicle_no, String License_no, String Cab_type, String UPI_Id) {
        this.Driver_email = Driver_email;
        this.Driver_name = Driver_name;
        this.Driver_phone = Driver_phone;
        this.Vehicle_no = Vehicle_no;
        this.License_no = License_no;
        this.UPI_Id = UPI_Id;
        this.Cab_type = Cab_type;
        this.Average_rating = 0.0;
        this.Cab_status = "Available";
        this.source = new Latlng("0","0"); // Finally current location will be a string like "0.0000 0.0000 0.0000 0.0000"
        // Split that, first two are latitude rest longitude
    }

    // GETTERS
    public String getCab_status() {
        return Cab_status;
    }
    public Double getAverage_rating() {
        return Average_rating;
    }
    public String getCab_type() {
        return Cab_type;
    }
    public Latlng getSource() {
        return source;
    }
    public String getDriver_phone() {
        return Driver_phone;
    }
    public String getDriver_name() {
        return Driver_name;
    }
    public String getDriver_email() {
        return Driver_email;
    }
    public String getUPI_Id() {
        return UPI_Id;
    }
    public String getVehicle_no() {
        return Vehicle_no;
    }

    // SETTERS

    public void setCab_status(String cab_status) {
        Cab_status = cab_status;
    }
    public void setAverage_rating(Double average_rating) {
        Average_rating = average_rating;
    }
    public void setCab_type(String cab_type) {
        Cab_type = cab_type;
    }
    public void setVehicle_no(String vehicle_no) {
        Vehicle_no = vehicle_no;
    }
    public void setSource(Latlng newSource) {
        source = newSource;
    }
    public void setLicense_no(String license_no) {
        License_no = license_no;
    }
}
