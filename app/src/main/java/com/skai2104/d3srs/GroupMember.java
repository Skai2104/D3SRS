package com.skai2104.d3srs;

public class GroupMember {
    private String mName, mEmail, mPhone, mNickname, mUserId, mDocId, mType;

    public GroupMember() {
    }

    public GroupMember(String name, String email, String phone, String nickname, String userId, String docId, String type) {
        mName = name;
        mEmail = email;
        mPhone = phone;
        mNickname = nickname;
        mUserId = userId;
        mDocId = docId;
        mType = type;
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
}
