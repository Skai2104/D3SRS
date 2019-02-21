package com.skai2104.d3srs;

public class User {
    // datetime is the date and time where the latest status update is sent
    String name, email, phone, userId, status, datetime;
    private double latitude, longitude;

    public User() {}

    public User(String name, String email, String phone, String status, double latitude, double longitude, String datetime) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public User WithId(String id) {
        userId = id;
        return this;
    }
}
