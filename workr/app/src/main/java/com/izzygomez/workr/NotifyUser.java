package com.izzygomez.workr;

import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pdgraham on 4/22/15.
 */
public class NotifyUser {

    ArrayList assignments = new ArrayList<Assignment>(); // Subject, EstimatedTime, DueDate, Priority
    int hourOfDayBegins = 8; // For 8 AM or w/e
    int hoursInADay = 24;
    Calendar projectDueDate = Calendar.getInstance();
    Assignment assignmentOne = new Assignment("6.s198 Project 2 Beta", 15, projectDueDate,true);
    Assignment assignmentTwo = new Assignment("6.s198 Project 4 Beta", 2, projectDueDate,true);
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    /*
* Total time starting from now (when function is called) till the end of the given day,
** Currently assumes the two days are of the same month.
 */
    public int calculateTotalTime(Calendar timeTill ) {
        Calendar currentTime = Calendar.getInstance();
        int hourOfDay = currentTime.get(Calendar.HOUR_OF_DAY);

        // sanity check
        if (timeTill.before(currentTime)) {
            int noTime = 0;
            return noTime;
        }

        // 24 hours in a day - the max of 8 AM or the current hour
        int numberOfFreeHoursInDay = hoursInADay - Math.max(hourOfDay, hourOfDayBegins);

        int daysTill;
        if (timeTill.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH)) {
            daysTill = timeTill.get(Calendar.DAY_OF_MONTH) - currentTime.get(Calendar.DAY_OF_MONTH);
        } else {
            // Hard coded to assume 31 days in the month, this is totally wrong
            // sketchy corner case of the end of the month
            int daysLeftInMonth = 31 - currentTime.get(Calendar.DAY_OF_MONTH);
            int daysTillInNextMonth = timeTill.get(Calendar.DAY_OF_MONTH);
            int daysInMonthsBetween = timeTill.get(Calendar.MONTH) - currentTime.get(Calendar.MONTH);
            daysTill = daysLeftInMonth + daysTillInNextMonth + daysInMonthsBetween*31;
        }

        int numberOfTotalFreeHours = numberOfFreeHoursInDay + daysTill*(hoursInADay-hourOfDayBegins);

        return numberOfTotalFreeHours;
    }

    /*
        Implemented currently only with the assignments
     */
    public int calculateFreeTime(int totalFreeHours, ArrayList<Assignment> assignmentsTemp) {
        int hoursFromAssignments = 0;

        for (Assignment assignment : ((ArrayList<Assignment>) assignmentsTemp)) {
            hoursFromAssignments += assignment.getEstimatedTime();
        }

        int freeTime = totalFreeHours - hoursFromAssignments;

        return freeTime;
    }

    public int percentOfTimeLeft (Assignment currentAssignment) {
        int timeLeft = calculateTotalTime(currentAssignment.getDueDate());

        ArrayList<Assignment> assignmentsAlsoDue = new ArrayList<Assignment>();

        for(Assignment assignment : (ArrayList<Assignment>)assignments) {
            if (assignment != currentAssignment && !currentAssignment.getDueDate().before(assignment)) {
                assignmentsAlsoDue.add(assignment);
            }
        }

        int freeTimeLeft = calculateFreeTime(timeLeft, assignmentsAlsoDue);
        return (currentAssignment.getEstimatedTime()*100)/(freeTimeLeft);
    }

    /*
       Returns a boolean for if a notification should be sent
     */
    public boolean notifyUser(Assignment currentAssignment) {
        ArrayList highPriorityIntervals = new ArrayList<Integer>();
        highPriorityIntervals.add(100);
        highPriorityIntervals.add(90);
        highPriorityIntervals.add(80);
        highPriorityIntervals.add(70);


        ArrayList lowPriorityIntervals = new ArrayList<Integer>();
        lowPriorityIntervals.add(100);
        lowPriorityIntervals.add(90);

        Boolean notifyUser = false;

        int percentLeft = percentOfTimeLeft(currentAssignment);

        if (currentAssignment.getHighPriority() && highPriorityIntervals.contains(percentLeft)) {
            notifyUser = true;
        } else if(lowPriorityIntervals.contains(percentLeft)) {
            notifyUser = true;
        }

        return notifyUser;
    }

//    public void timeLeft (View view) {
////        ((TextView)findViewById(R.id.textView)).setText("Assignment One Due on: " + sdf.format(assignmentOne.getDueDate().getTime()) + " is " + calculateTotalTime(assignmentOne.getDueDate()) + " hours left");
//        ((TextView)findViewById(R.id.textView)).setText("You have " + percentOfTimeLeft(assignmentOne));
//    }

}
