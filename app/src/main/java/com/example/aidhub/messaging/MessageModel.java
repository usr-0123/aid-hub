package com.example.aidhub.messaging;

import java.util.List;

public class MessageModel {
    private String messageId;
    private String mediaUrl;
    private String message;
    private String senderId;
    private String timestamp;
    private String type;
    private List<String> readBy;

    // Constructor
    public MessageModel(String messageId, String mediaUrl, String message, String senderId, String timestamp, String type, List<String> readBy) {
        this.messageId = messageId;
        this.mediaUrl = mediaUrl;
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
        this.readBy = readBy;
    }

    // Default constructor for Firebase
    public MessageModel() {}

    // Getters and setters
    public String getMessageId() {return messageId;}

    public void setMessageId(String messageId) {this.messageId = messageId;}

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }
}