package com.example.aidhub.aid;

import java.util.List;

public class AidRequestModel {
    private String requestId;
    private String service;
    private String description;
    private String latitude;
    private String longitude;
    private String seekerId;
    private Boolean approved;
    private List<String> readBy;

    // Default constructor required for calls to DataSnapshot.getValue(AidRequestModel.class)
    public AidRequestModel() {}

    // Parameterized constructor
    public AidRequestModel(String requestId, String service, String description, String latitude, String longitude, String seekerId,  Boolean approved, List<String> readBy) {
        this.requestId = requestId;
        this.service = service;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.seekerId = seekerId;
        this.approved = approved;
        this.readBy = readBy;
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

    public String getSeekerId() {return seekerId;}

    public void setSeekerId(String seekerId) {this.seekerId = seekerId;}

    public Boolean getApproved() {return approved;}

    public void setApproved(Boolean approved) {this.approved = approved;}

    public List<String> getReadBy() {return readBy;}

    public void setReadBy(List<String> readBy) {this.readBy = readBy;}
}
