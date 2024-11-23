package com.example.goalgetter;

import java.util.List;

public class GroupChat {
    private String groupId;
    private String groupName;
    private List<User> users;
    private String leaderId;
    private String leaderName;
    private String leaderEmail;
    private String latestMessage;
    private long latestTimestamp;

    public GroupChat() {
    }

    public GroupChat(String groupId, String groupName, List<User> users, String leaderId, String leaderName, String leaderEmail, String latestMessage, long latestTimestamp) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.users = users;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.leaderEmail = leaderEmail;
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

    public String getLeaderId() {
        return leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public String getLeaderEmail() {
        return leaderEmail;
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

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public void setLeaderEmail(String leaderEmail) {
        this.leaderEmail = leaderEmail;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public void setLatestTimestamp(long latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}
