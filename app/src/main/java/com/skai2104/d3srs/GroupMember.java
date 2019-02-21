package com.skai2104.d3srs;

public class GroupMember {
    // mDatetime is the date and time where the latest status update is sent
    private String mName, mEmail, mPhone, mNickname, mUserId, mDocId, mType, mStatus, mDateTime;
    private double mLatitude, mLongitude;

    public GroupMember() {
    }

    public GroupMember(String name, String email, String phone, String nickname, String userId, String docId, String type, String status, double latitude, double longitude, String datetime) {
        mName = name;
        mEmail = email;
        mPhone = phone;
        mNickname = nickname;
        mUserId = userId;
        mDocId = docId;
        mType = type;
        mStatus = status;
        mLatitude = latitude;
        mLongitude = longitude;
        mDateTime = datetime;
    }

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getDocId() {
        return mDocId;
    }

    public String getType() {
        return mType;
    }

    public String getStatus() {
        return mStatus;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setDocId(String docId) {
        mDocId = docId;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public void setDateTime(String dateTime) {
        mDateTime = dateTime;
    }
}
