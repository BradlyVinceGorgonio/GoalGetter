package com.example.goalgetter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Task {
    private String courseName;
    private String taskType;
    private String dateDue;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d-M-yyyy", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("MMMM d EEEE", Locale.US);

    public Task() {}

    public Task(String courseName, String taskType, String dateDue) {
        this.courseName = courseName;
        this.taskType = taskType;
        this.dateDue = dateDue;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDateDue() {
        return dateDue;
    }

    public void setDateDue(String dateDue) {
        this.dateDue = dateDue;
    }

    public Date getDateDueAsDate() {
        try {
            return DATE_FORMAT.parse(dateDue);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFormattedDate() {
        Date date = getDateDueAsDate();
        if (date != null) {
            return DISPLAY_DATE_FORMAT.format(date);
        }
        return "";
    }

    public boolean isOnDate(int day, int month, int year) {
        Date dueDate = getDateDueAsDate();
        if (dueDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dueDate);
            int taskDay = calendar.get(Calendar.DAY_OF_MONTH);
            int taskMonth = calendar.get(Calendar.MONTH) + 1;
            int taskYear = calendar.get(Calendar.YEAR);
            return (taskDay == day && taskMonth == month && taskYear == year);
        }
        return false;
    }
}
