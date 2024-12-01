package com.example.aidhub.amenities;

public class amenityModel {
    private String amenityId;
    private String name;
    private String category;
    private String description;
    private String latitude;
    private String longitude;

    private amenityModel() {}

    public amenityModel(String amenityId, String name, String category, String description, String latitude, String longitude) {
        this.amenityId = amenityId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(String amenityId) {
        this.amenityId = amenityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
