package com.skai2104.d3srs;

public class SOS {
    private String datetime, mFrom, mFromId, mLatitude, mLongitude, mAddress, mDocId;

    public SOS() {
    }

    public SOS(String dateTime, String from, String fromId, String latitude, String longitude) {
        this.datetime = dateTime;
        mFrom = from;
        mFromId = fromId;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getDateTime() {
        return datetime;
    }

    public String getFrom() {
        return mFrom;
    }

    public String getFromId() {
        return mFromId;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getDocId() {
        return mDocId;
    }



    public void setDateTime(String dateTime) {
        this.datetime = dateTime;
    }

    public void setFrom(String from) {
        mFrom = from;
    }

    public void setFromId(String fromId) {
        mFromId = fromId;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public void setDocId(String docId) {
        mDocId = docId;
    }
}
