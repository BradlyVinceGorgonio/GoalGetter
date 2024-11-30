package com.example.goalgetter;

public class GroupNotification {
    public static final int TYPE_CHAT = 0;
    public static final int TYPE_TASK = 1;

    private String groupId;
    private String groupName;
    private String messageId;
    private String messageText;
    private String senderName;
    private String taskId;
    private String taskTitle;
    private long timestamp;
    private int notificationType;

    public GroupNotification(String groupId, String groupName, String messageId, String messageText, String senderName, long timestamp) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.messageId = messageId;
        this.messageText = messageText;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.notificationType = TYPE_CHAT;
    }

    public GroupNotification(String taskId, String taskTitle, long timestamp) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.timestamp = timestamp;
        this.notificationType = TYPE_TASK;
    }

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

    public String getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNotificationType() {
        return notificationType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GroupNotification)) return false;
        GroupNotification that = (GroupNotification) obj;
        return (notificationType == TYPE_CHAT && messageId.equals(that.messageId)) ||
                (notificationType == TYPE_TASK && taskId.equals(that.taskId));
    }

    @Override
    public int hashCode() {
        return notificationType == TYPE_CHAT ? messageId.hashCode() : taskId.hashCode();
    }
}
