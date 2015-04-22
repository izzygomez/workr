package edu.mit.pdgraham.remindertest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pdgraham on 4/19/15.
 */
public class Assignment {


    private String subject;
    private int estimatedTime;
    private Calendar dueDate;
    private Boolean highPriority;

    public Assignment(String assignmentName, int timeToFinish, Calendar turnInDate, Boolean priority) {
        this.subject = assignmentName;
        this.estimatedTime = timeToFinish;
        this.dueDate = turnInDate;
        this.highPriority = priority;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(Boolean highPriority) {
        this.highPriority = highPriority;
    }

}
