package com.techtop.onehyreapp;

public class TripData
{
    private String dateTxt;
    private String vnoTxt;
    private String phoneTxt;
    private String oneKmRateTxt;
    private String  twoKmRate;
    private String  startTime;
    private String  waitTime;
    private String  waitRate;
    private String  distance;
    private String totFare;
    private String totHireTime;
    private String endTime;
    private String timeMill;
    private String clientName;
    private String clientMail;
    private String clientPhone;
    private String tripID;
    private String startPointAddress;
    private String endPointAddress;
    private String coordinatesLatLan;
    private boolean cardExpanded;

    public TripData() {}

    public TripData(String dateTxt, String vnoTxt, String phoneTxt, String oneKmRateTxt, String twoKmRate, String startTime, String waitTime, String waitRate, String distance, String totFare, String totHireTime, String endTime, String timeMill, String clientName, String clientMail, String clientPhone, String tripCode, String startPointAddress, String endPointAddress, String coordinatesLatLan) {
        this.dateTxt = dateTxt;
        this.vnoTxt = vnoTxt;
        this.phoneTxt = phoneTxt;
        this.oneKmRateTxt = oneKmRateTxt;
        this.twoKmRate = twoKmRate;
        this.startTime = startTime;
        this.waitTime = waitTime;
        this.waitRate = waitRate;
        this.distance = distance;
        this.totFare = totFare;
        this.totHireTime = totHireTime;
        this.endTime = endTime;
        this.timeMill = timeMill;
        this.clientName = clientName;
        this.clientMail = clientMail;
        this.clientPhone = clientPhone;
        this.tripID = tripCode;
        this.startPointAddress = startPointAddress;
        this.endPointAddress = endPointAddress;
        this.coordinatesLatLan = coordinatesLatLan;
        this.cardExpanded = false;
    }

    public String getTimeMill() {
        return timeMill;
    }

    public void setTimeMill(String timeMill) {
        this.timeMill = timeMill;
    }


    public String getDateTxt() {
        return dateTxt;
    }

    public void setDateTxt(String dateTxt) {
        this.dateTxt = dateTxt;
    }

    public String getVnoTxt() {
        return vnoTxt;
    }

    public void setVnoTxt(String vnoTxt) { this.vnoTxt = vnoTxt; }

    public String getPhoneTxt() {
        return phoneTxt;
    }

    public void setPhoneTxt(String phoneTxt) {
        this.phoneTxt = phoneTxt;
    }

    public String getOneKmRateTxt() {
        return oneKmRateTxt;
    }

    public void setOneKmRateTxt(String oneKmRateTxt) {
        this.oneKmRateTxt = oneKmRateTxt;
    }

    public String getTwoKmRate() {
        return twoKmRate;
    }

    public void setTwoKmRate(String twoKmRate) {
        this.twoKmRate = twoKmRate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }

    public String getWaitRate() {
        return waitRate;
    }

    public void setWaitRate(String waitRate) {
        this.waitRate = waitRate;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTotFare() {
        return totFare;
    }

    public void setTotFare(String totFare) {
        this.totFare = totFare;
    }

    public String getTotHireTime() {
        return totHireTime;
    }

    public void setTotHireTime(String totHireTime) {
        this.totHireTime = totHireTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getClientName() { return clientName; }

    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientMail() { return clientMail; }

    public void setClientMail(String clientMail) { this.clientMail = clientMail; }

    public String getClientPhone() { return clientPhone; }

    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getTripID() { return tripID; }

    public void setTripID(String tripID) { this.tripID = tripID; }

    public String getStartPointAddress() {
        return startPointAddress;
    }

    public void setStartPointAddress(String startPointAddress) { this.startPointAddress = startPointAddress; }

    public String getEndPointAddress() { return endPointAddress; }

    public void setEndPointAddress(String endPointAddress) { this.endPointAddress = endPointAddress; }

    public String getCoordinatesLatLan() { return coordinatesLatLan; }

    public void setCoordinatesLatLan(String coordinatesLatLan) { this.coordinatesLatLan = coordinatesLatLan; }

    public boolean isCardExpanded() {
        return cardExpanded;
    }

    public void setCardExpanded(boolean cardExpanded) {
        this.cardExpanded = cardExpanded;
    }
}
