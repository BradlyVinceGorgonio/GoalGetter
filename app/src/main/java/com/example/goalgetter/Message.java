package com.example.goalgetter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String messageId;  // Unique message ID
    private String senderId;
    private String senderName;
    private String receiverId;
    private String messageText;
    private String imageUrl;
    private long timestamp;
    private String messageType;  // Differentiates between text and image

    public Message() {
    }

    public Message(String senderId, String senderName, String receiverId, String messageText, String imageUrl, long timestamp, String messageType) {
        this.messageId = generateMessageId();  // Generate unique message ID
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.messageType = messageType;  // Store the type of message (text/image)
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    // Helper method to generate a unique message ID based on timestamp
    private String generateMessageId() {
        return String.valueOf(System.currentTimeMillis());
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getUserName() {
        return senderName != null && !senderName.isEmpty() ? senderName : "Unknown User";
    }
}
