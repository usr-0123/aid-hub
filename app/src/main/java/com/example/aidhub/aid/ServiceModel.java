package com.example.aidhub.aid;

public class ServiceModel {
    private String id;
    private String title;
    private String description;

    // Required empty constructor for Firebase
    public ServiceModel() {}

    public ServiceModel(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
