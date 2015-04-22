package edu.mit.pdgraham.remindertest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ArrayList assignments = new ArrayList<Assignment>(); // Subject, EstimatedTime, DueDate, Priority
    int hourOfDayBegins = 8; // For 8 AM or w/e
    int hoursInADay = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
        int timeLeft = calculateTotalTime(currentAssignment.getDueDate());

        ArrayList<Assignment> assignmentsAlsoDue = new ArrayList<Assignment>();

        for(Assignment assignment : (ArrayList<Assignment>)assignments) {
            if (assignment != currentAssignment && currentAssignment.getDueDate().after(assignment)) {
                assignmentsAlsoDue.add(assignment);
            }
        }

        int freeTimeLeft = calculateFreeTime(timeLeft, assignmentsAlsoDue);

        if (currentAssignment.getHighPriority() && highPriorityIntervals.contains(currentAssignment.getEstimatedTime()/freeTimeLeft)) {
            notifyUser = true;
        } else if(lowPriorityIntervals.contains(currentAssignment.getEstimatedTime()/freeTimeLeft)) {
            notifyUser = true;
        }

        return notifyUser;
    }


}


