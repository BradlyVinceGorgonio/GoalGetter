package com.example.goalgetter;

import java.util.List;

public class GroupChat {
    private String groupId;
    private String groupName;
    private List<User> users;
    private String latestMessage;
    private long latestTimestamp;

    public GroupChat() {
    }

    public GroupChat(String groupId, String groupName, List<User> users, String latestMessage, long latestTimestamp) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.users = users;
        this.latestMessage = latestMessage;
        this.latestTimestamp = latestTimestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public long getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public void setLatestTimestamp(long latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}
