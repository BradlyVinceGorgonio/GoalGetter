package com.example.goalgetter;

public class Message {
    private String senderId;
    private String sender;
    private String receiver; // Optional, you can use it if needed
    private String text;
    private long timestamp;

    public Message() {}

    public Message(String senderId, String sender, String receiver, String text, long timestamp) {
        this.senderId = senderId;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
