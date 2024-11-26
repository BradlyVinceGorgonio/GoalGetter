package com.example.goalgetter;

public class GroupNotification {
    private String groupId;
    private String groupName;
    private String messageId;
    private String messageText;
    private String senderName;
    private long timestamp;

    public GroupNotification(String groupId, String groupName, String messageId, String messageText, String senderName, long timestamp) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.messageId = messageId;
        this.messageText = messageText;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GroupNotification)) return false;
        GroupNotification that = (GroupNotification) obj;
        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }
}
