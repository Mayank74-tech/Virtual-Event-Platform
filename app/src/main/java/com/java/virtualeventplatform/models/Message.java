package com.java.virtualeventplatform.models;

public class Message {
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;
    private String eventId;

    // 🔹 Empty constructor (needed for Firestore)
    public Message() {}

    // 🔹 Full constructor
    public Message(String senderId, String senderName, String message, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
    }

    // 🔹 Getters & Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getEventId()
    { return eventId; }

    public void setEventId(String eventId)
    { this.eventId = eventId; }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
