package com.example.goalgetter;

import java.util.List;

public class GroupChat {
    private String groupId;
    private String groupName;
    private List<User> users;

    public GroupChat() {
    }

    public GroupChat(String groupId, String groupName, List<User> users) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.users = users;
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

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
