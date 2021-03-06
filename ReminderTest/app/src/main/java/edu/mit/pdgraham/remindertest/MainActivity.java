package edu.mit.pdgraham.remindertest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ArrayList assignments = new ArrayList<Assignment>(); // Subject, EstimatedTime, DueDate, Priority
    int hourOfDayBegins = 8; // For 8 AM or w/e
    int hoursInADay = 24;
    Calendar projectDueDate = Calendar.getInstance();
    Assignment assignmentOne = new Assignment("6.s198 Project 2 Beta", 15, projectDueDate,true);
    Assignment assignmentTwo = new Assignment("6.s198 Project 4 Beta", 2, projectDueDate,true);
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");

//    Intent resultIntent = new Intent(this, ResultActivity.class);
//    // Because clicking the notification opens a new ("special") activity, there's
//// no need to create an artificial back stack.
//    PendingIntent resultPendingIntent =
//            PendingIntent.getActivity(
//                    this,
//                    0,
//                    resultIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//            );

//    // Sets an ID for the notification
//    int mNotificationId = 001;
//    // Gets an instance of the NotificationManager service
//    NotificationManager mNotifyMgr =
//            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//    // Builds the notification and issues it.
//    mNotifyMgr.notify(mNotificationId, mBuilder.build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectDueDate.set(2015, 3, 22);
        System.out.println(sdf.format(projectDueDate.getTime()));
//        projectDueDate.set()
        assignments.add(assignmentOne);
        assignments.add(assignmentTwo);
        ((TextView) findViewById(R.id.textView)).setText(assignments.toString());
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

    public void timeLeft (View view) {
//        ((TextView)findViewById(R.id.textView)).setText("Assignment One Due on: " + sdf.format(assignmentOne.getDueDate().getTime()) + " is " + calculateTotalTime(assignmentOne.getDueDate()) + " hours left");
        ((TextView)findViewById(R.id.textView)).setText("You have " + percentOfTimeLeft(assignmentOne));
    }


}


