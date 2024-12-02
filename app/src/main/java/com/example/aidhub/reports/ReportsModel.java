package com.example.aidhub.reports;

import java.util.List;

public class ReportsModel {
    private String reportId;
    private String requestId;
    private String service;
    private String description;
    private String latitude;
    private String longitude;
    private String seekerId;
    private Boolean approved;
    private List<String> readBy;
    private String providerId;
    private String providedDate;
    private String reportDescription;

    // Constructor with all fields
    public ReportsModel(String reportId, String requestId, String service, String description, String latitude, String longitude, String seekerId, Boolean approved, List<String> readBy, String providerId, String providedDate, String reportDescription) {
        this.requestId = requestId;
        this.service = service;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.seekerId = seekerId;
        this.approved = approved;
        this.readBy = readBy;
        this.providerId = providerId;
        this.providedDate = providedDate;
        this.reportDescription = reportDescription;
    }

    // Default constructor
    public ReportsModel() {}

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

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

    public String getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(String seekerId) {
        this.seekerId = seekerId;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvidedDate() {
        return providedDate;
    }

    public void setProvidedDate(String providedDate) {
        this.providedDate = providedDate;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }
}
