package com.example.aidhub.chats;

import java.util.List;

public class ChatModel {
    private String chatId;
    private long createdAt;
    private List<String> members;
    private long updatedAt;

    // Constructor
    public ChatModel(String chatId, long createdAt, List<String> members, long updatedAt) {
        this.chatId = chatId;
        this.createdAt = createdAt;
        this.members = members;
        this.updatedAt = updatedAt;
    }

    // Default constructor for Firebase
    public ChatModel() {}

    // Getters and setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId)
    {
        this.chatId = chatId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}