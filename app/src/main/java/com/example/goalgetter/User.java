package com.example.goalgetter;

public class User {
    private String uid;
    private String email;
    private String name;
    private boolean selected;

    public User() {}

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.selected = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
