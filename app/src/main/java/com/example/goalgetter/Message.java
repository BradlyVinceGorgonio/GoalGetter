package com.example.goalgetter;

public class Message {
    private String senderName;
    private String messageText;

    public Message() {
    }

    public Message(String senderName, String messageText) {
        this.senderName = senderName;
        this.messageText = messageText;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }
}