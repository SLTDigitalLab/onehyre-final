package com.techtop.onehyreapp;

public class SystemStatus {

    private String currentStatus;
    private String errMsg;

    public SystemStatus() {
    }

    public SystemStatus(String currentStatus, String errMsg) {
        this.currentStatus = currentStatus;
        this.errMsg = errMsg;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
