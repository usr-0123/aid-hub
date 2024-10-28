package com.example.aidhub.groups;

import java.util.List;

public class GroupModel {
    private String groupId;
    private String adminId;
    private String groupName;
    private long createdAt;
    private long updatedAt;
    private List<String> members;

    public GroupModel() {
        // Empty constructor required for Firebase
    }

    public GroupModel(String groupId,String adminId, String groupName, long createdAt, long updatedAt, List<String> members) {
        this.groupId = groupId;
        this.adminId = adminId;
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.members = members;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getMembers() {
        return members;
    }
}