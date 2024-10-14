package com.example.aidhub.users;

public class UserModel {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImage;
    private long createdAt;
    private long lastSeen;
    private String userType;

    // Constructor
    public UserModel(String userId, String firstName, String lastName, String email, String phone, String profileImage, long createdAt, long lastSeen, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.lastSeen = lastSeen;
        this.userType = userType;
    }

    // Default constructor (for Firebase)
    public UserModel() {}

    // Getters and setters
    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {return phone;}

    public void setPhone(String phone){this.phone = phone;}

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}