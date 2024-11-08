package com.example.goalgetter;

public class PendingTaskList {
    private String priorityMode;  //YES OR NO
    private String courseName;
    private String dueDate;
    private String taskType;
    private String dueTime;
    private String UID;  // User ID
    private String taskID;  // Task ID

    // Constructor
    public PendingTaskList(String priorityMode, String courseName, String dueDate, String taskType, String dueTime, String UID, String taskID) {
        this.priorityMode = priorityMode;
        this.courseName = courseName;
        this.dueDate = dueDate;
        this.taskType = taskType;
        this.dueTime = dueTime;
        this.UID = UID;
        this.taskID = taskID;
    }

    // Getters and setters
    public String getPriorityMode() {
        return priorityMode;
    }

    public void setPriorityMode(String priorityMode) {
        this.priorityMode = priorityMode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
}