package com.example.goalgetter;

public class Message {
    private String userId;
    private String userName;
    private String imageUrl;
    private String messageText;
    private long timestamp;

    public Message() {
    }

    public Message(String userId, String userName, String imageUrl, String messageText, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
