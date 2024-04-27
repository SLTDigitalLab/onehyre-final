package com.techtop.onehyreapp;

public class Employee
{
    private String name;
    private String phoneNumber;
    private String vehicleNo;
    private String driverStatus;
    private String liveLocation;
    private String vType;
    private String vModel;
    private String accountStatus;
    private String companyID;
    private String isApproved;
    private String nic;

    // additional variables
    private String speed;
    private  String totalDistance;

    private boolean isPlatformCharges;
    private String billingDate;
    private String billingOrderID;

    public Employee() {}

    public Employee(String name, String phoneNumber, String vehicleNo, String driverStatus, String liveLocation, String vType, String vModel, String accountStatus, String companyID, String isApproved, String nic,boolean isPlatformCharges, String billingDate, String billingOrderID) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.vehicleNo = vehicleNo;
        this.driverStatus = driverStatus;
        this.liveLocation = liveLocation;
        this.vType = vType;
        this.vModel = vModel;
        this.accountStatus = accountStatus;
        this.companyID = companyID;
        this.isApproved = isApproved;
        this.nic = nic;
        this.isPlatformCharges = isPlatformCharges;
        this.billingDate = billingDate;
        this.billingOrderID = billingOrderID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public String getLiveLocation() { return liveLocation; }

    public void setLiveLocation(String liveLocation) {
        this.liveLocation = liveLocation;
    }

    public String getvType() { return vType; }

    public void setvType(String vType) { this.vType = vType; }

    public String getvModel() { return vModel; }

    public void setvModel(String vModel) { this.vModel = vModel; }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(String isApproved) {
        this.isApproved = isApproved;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }


    // additional getters and setters
    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public boolean isPlatformCharges() {
        return isPlatformCharges;
    }

    public void setPlatformCharges(boolean platformCharges) {
        isPlatformCharges = platformCharges;
    }

    public String getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(String billingDate) {
        this.billingDate = billingDate;
    }

    public String getBillingOrderID() {
        return billingOrderID;
    }

    public void setBillingOrderID(String billingOrderID) {
        this.billingOrderID = billingOrderID;
    }






}
