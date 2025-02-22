package com.smartuis.module.domian.entity;

import java.time.Instant;

public class Header {
    private String userUUID;

    private String deviceId;

    private Instant timeStamp;

    private String location;

    public Header(String userUUID, String deviceId, String location){
        this.userUUID = userUUID;
        this.deviceId = deviceId;
        timeStamp = Instant.now();
        this.location = location;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Header{" +
                "userUUID='" + userUUID + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", timeStamp=" + timeStamp +
                ", location='" + location + '\'' +
                '}';
    }
}
