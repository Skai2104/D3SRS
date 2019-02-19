package com.skai2104.d3srs;

public class SightingInfo {
    private String datetime, content, reportPersonName, reportPersonId, mDocId, location;

    public SightingInfo() {
    }

    public SightingInfo(String datetime, String content, String reportPersonName, String reportPersonId, String location) {
        this.datetime = datetime;
        this.content = content;
        this.reportPersonName = reportPersonName;
        this.reportPersonId = reportPersonId;
        this.location = location;
    }

    public String getDateTime() {
        return datetime;
    }

    public String getContent() {
        return content;
    }

    public String getReportPersonName() {
        return reportPersonName;
    }

    public String getReportPersonId() {
        return reportPersonId;
    }

    public String getDocId() {
        return mDocId;
    }

    public String getLocation() {
        return location;
    }

    public void setDateTime(String dateTime) {
        this.datetime = dateTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReportPersonName(String reportPersonName) {
        this.reportPersonName = reportPersonName;
    }

    public void setReportPersonId(String reportPersonId) {
        this.reportPersonId = reportPersonId;
    }

    public void setDocId(String docId) {
        mDocId = docId;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
