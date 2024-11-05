package com.example.aidhub.aid;

public class AidRequestModel {
    private String requestId;
    private String service;
    private String description;
    private String latitude;
    private String longitude;

    // Default constructor required for calls to DataSnapshot.getValue(AidRequestModel.class)
    public AidRequestModel() {}

    // Parameterized constructor
    public AidRequestModel(String requestId, String service, String description, String latitude, String longitude) {
        this.requestId = requestId;
        this.service = service;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "AidRequestModel{" +
                "requestId='" + requestId + '\'' +
                ", service='" + service + '\'' +
                ", description='" + description + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}