package com.izzygomez.workr;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.izzygomez.workr.NotifyUser.calculateFreeTime;

public class MainActivity extends ActionBarActivity {
    ArrayList<ListedItem> listItems = new ArrayList<ListedItem>();
    ArrayAdapter<ListedItem> adapter;
    boolean deleteMode = false;
    int lastClickedRow = 10000;
    ArrayList<String> lastClickedRowArray = new ArrayList<String>(); //Array of Strings to pass to TaskInputScreen to preload input boxes during edits
    ArrayList<String> taskInputData = new ArrayList<String>(); //data that comes from the TaskInputScreen to be displayed
    ArrayList<Assignment> usersAssignments = new ArrayList<Assignment>();//title of each assignment
    List<Event> usersEvents = new ArrayList<>();
    ListedItem currentlySelectedListItem;
    View currentlySelectedRow;
    ListView listView;
    CardArrayAdapter cardArrayAdapter;
    int pos;
    public View row;

    // <Izzy's variables>
    /**
     * A Calendar service object used to query or modify calendars via the
     * Calendar API. Note: Do not confuse this class with the
     * com.google.api.services.calendar.model.Calendar class.
     */
    protected com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};

    // </Izzy's variables>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        populateListItemsFromFile();

        ListView taskListView = (ListView) findViewById(R.id.listViewOfTasks);
        adapter = new ArrayAdapter<ListedItem>(this, android.R.layout.simple_list_item_1, listItems);


        listView = (ListView) findViewById(R.id.card_listView);
        taskListView.addHeaderView(new View(this));
        taskListView.addFooterView(new View(this));
        cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), R.layout.list_item_view, listItems);

        taskListView.setAdapter(cardArrayAdapter);

//        cardArrayAdapter.notifyDataSetChanged();
        cardArrayAdapter.updateList(listItems);
        updateStorage();
        Log.d("listItems",listItems.toString());

//        taskListView.setAdapter(adapter);
//        Log.d("view", findViewById(R.id.cardList).toString());

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                row = arg1;
                position -= 1;
                pos = position;
                Log.d("first pos: ", Integer.toString(position));
                if (row != null){
                    if (!listItems.get(position).isSelected()) {
                        Log.d("second pos: ", Integer.toString(position));
                        if (currentlySelectedListItem == null) {
                            row.setBackgroundResource(R.drawable.card_state_pressed);
                            currentlySelectedListItem = listItems.get(position);
                            Log.d("third pos: ", Integer.toString(position));
                            currentlySelectedRow = arg1;

                        }
                        else{
                            listItems.get(listItems.indexOf(currentlySelectedListItem)).toggleSelection();
                            currentlySelectedRow.setBackgroundResource(0);
//                            currentlySelectedRow.setPressed(false);
                            row.setBackgroundResource(R.drawable.card_state_pressed);
                            currentlySelectedListItem = listItems.get(position);
                            Log.d("fourth pos: ", Integer.toString(position));
                            currentlySelectedRow = arg1;
                        }

                    }
                    else{
                        row.setBackgroundResource(0);
                        currentlySelectedListItem = null;
                    }
                }
                listItems.get(position).toggleSelection();
                Log.d("fifth pos: ", Integer.toString(position));
                Log.d("selected", Boolean.toString(listItems.get(position).isSelected()));


//                v.setBackgroundResource(R)
                lastClickedRow = position;
                String[] lastClickedArrayString = arg0.getItemAtPosition(position + 1).toString().split(" ");
                Log.d("six pos: ", Integer.toString(position));
                lastClickedRowArray = new ArrayList<String>();
                for(String s: lastClickedArrayString){
                    lastClickedRowArray.add(s);
                }
            }
        });

//
//        });


        // IZZY'S CODE
        // Initialize credentials and calendar service
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Workr")
                .build();

        // PHILLY'S CODE
        calcFreeTime();
    }


    public void goToTaskInputScreen(){
        Intent taskInputIntent = new Intent(this, TaskInputScreen.class);
        taskInputIntent.putExtra("taskData", lastClickedRowArray);
        startActivityForResult(taskInputIntent, 5);
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

    public void addToList(ArrayList<String> taskInputs){
        ListedItem listViewInput = new ListedItem(taskInputs.get(0), taskInputs.get(1), taskInputs.get(2), taskInputs.get(3));
        listItems.add(listViewInput);
        cardArrayAdapter.updateList(listItems);

//        cardArrayAdapter.notifyDataSetChanged();
        updateStorage();
        lastClickedRowArray = taskInputs;

    }

    public void clickedPlus(View v){

        lastClickedRowArray = new ArrayList<>();
        if(currentlySelectedRow != null){
            currentlySelectedRow.setBackgroundResource(0);
        }
        for (ListedItem item: listItems){
            if (item.isSelected()){
                item.toggleSelection();
            }
        }

        currentlySelectedListItem = null;
        calcFreeTime();
        goToTaskInputScreen();
    }


    public void deleteSelectedItem(View v){
//        Toast.makeText(this, Integer.toString(pos), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, findViewById(R.id.cardList).toString(),Toast.LENGTH_LONG).show();
        Assignment deletedAssignment = null;
        for (Assignment assignment: usersAssignments) {
            if (currentlySelectedListItem != null) {
                if (currentlySelectedListItem.toString().equals(assignment.toString())) {
//                    deletedAssignment = assignment;

                    usersAssignments.remove(usersAssignments.indexOf(assignment));
                    break;
                }
            }
        }
//        if (deletedAssignment != null) {
//            usersAssignments.remove(usersAssignments.indexOf(deletedAssignment));
//        }
        if(currentlySelectedListItem != null) {
            listItems.remove(currentlySelectedListItem);
        }
        calcFreeTime();

        currentlySelectedListItem = null;
        if (currentlySelectedRow != null) {
            currentlySelectedRow.setBackgroundResource(0);
        }
//        Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG).show();
//        cardArrayAdapter.notifyDataSetChanged();
        cardArrayAdapter.updateList(listItems);
        updateStorage();
    }

    public void editSelectedItem(View v){
        for (ListedItem item: listItems){
            if(item.isSelected()){
                for (Assignment assignment : usersAssignments) {
                    if (currentlySelectedListItem.toString().equals(assignment.toString())) {
                        usersAssignments.remove(usersAssignments.indexOf(assignment));
                        calcFreeTime();
                        break;
                    }
                }
                lastClickedRowArray = item.returnArrayList();
                goToTaskInputScreen();
            }
        }
    }

    public void updateStorage(){
        String FILENAME = "workr_file";
        deleteFile(FILENAME);
        String line = "";
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            for (ListedItem item : listItems) {
                line = item.toString() + "\n";
                fos.write(line.getBytes());
            }
            fos.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        String FILENAMEASSIGNMENT = "workr_file_assignments";
        deleteFile(FILENAMEASSIGNMENT);
        String lineAssignment = "";
        try {
            FileOutputStream fos = openFileOutput(FILENAMEASSIGNMENT, Context.MODE_PRIVATE);
            for (Assignment item : usersAssignments) {
                lineAssignment = item.toString() + "\n";
                fos.write(lineAssignment.getBytes());
            }
            fos.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    // TODO Remember to add some check that makes sure that you can't use colons in any of your task entries or transform them to something
    //else internally
    public void populateListItemsFromFile(){
        StringBuilder builder = new StringBuilder();
        int ch;
        String[] listItemsToStringArray;
        try{
            FileInputStream fis = openFileInput("workr_file");
            while((ch = fis.read()) != -1){
                builder.append((char)ch);
            }
            if (builder.toString().length() > 0) {
                listItemsToStringArray = builder.toString().split("\n");
                for (String listEntry : listItemsToStringArray) {
                    String[] listEntryParts = listEntry.split(" : ");
                    listItems.add(new ListedItem(listEntryParts[0], listEntryParts[1], listEntryParts[2], listEntryParts[3]));
                }
//                Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG);
//                cardArrayAdapter.updateList(listItems);
//                cardArrayAdapter.notifyDataSetChanged();
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
        StringBuilder builderAssignment = new StringBuilder();
        int chAssignment;
        String[] listItemsToStringArrayAssignment;
        try{
            FileInputStream fis = openFileInput("workr_file_assignments");
            while((chAssignment = fis.read()) != -1){
                builderAssignment.append((char)chAssignment);
            }
            if (builderAssignment.toString().length() > 0) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy"); // or mm/dd/yy or assume 2015?


                listItemsToStringArrayAssignment = builderAssignment.toString().split("\n");
                for (String listEntry : listItemsToStringArrayAssignment) {
                    String[] listEntryParts = listEntry.split(" : ");
                    try {
                        cal.setTime(sdf.parse(listEntryParts[2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    usersAssignments.add(new Assignment(listEntryParts[0], Integer.valueOf(listEntryParts[1]), cal, listEntryParts[3]));

                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public Assignment createAssignment() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy"); // or mm/dd/yy or assume 2015?

        try {
            cal.setTime(sdf.parse(taskInputData.get(2)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Assignment newAssignment = new Assignment(taskInputData.get(0), Integer.parseInt(taskInputData.get(1)),
                cal , taskInputData.get(3));
        return newAssignment;
    }

    // <Izzy's Methods>
    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
//    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshEventList();

        } else {
            // TODO do something here if necessary
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == 5) &&
                (resultCode == RESULT_OK)) {

            taskInputData = data.getExtras().getStringArrayList("returnData");

            Assignment newAssignment = createAssignment();


            lastClickedRowArray = new ArrayList<String>();
            addToList(taskInputData);
//            Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG);
            if (currentlySelectedListItem != null) {
                listItems.remove(currentlySelectedListItem);
                currentlySelectedListItem = null;
                currentlySelectedRow.setBackgroundResource(0);
//                cardArrayAdapter.notifyDataSetChanged();
                cardArrayAdapter.updateList(listItems);
                updateStorage();
            }
            usersAssignments.add(newAssignment);
            calcFreeTime();

        }

        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshEventList();

                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshEventList();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // mStatusText.setText("Account unspecified."); // Debugging purposes TODO delete this
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshEventList();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a list of calendar events to display. If the email
     * address isn't known yet, then call chooseAccount() method so the user
     * can pick an account.
     */
    private void refreshEventList() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new EventFetchTask(this).execute();

            } else {
                // mStatusText.setText("No network connection available."); // Debugging purposes TODO delete this
            }
        }
    }

    /**
     * Fill the event display with the given List of strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     * @param events a List of Strings to populate the event display with.
     */
    public void updateEventList(final List<Event> events) {
        usersEvents = events;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (events == null) {
                    // mStatusText.setText("Error retrieving events!"); // Debugging purposes TODO delete this
                } else if (events.size() == 0) {
                    // mStatusText.setText("No upcoming events found."); // Debugging purposes TODO delete this
                } else {
                    /*mStatusText.setText("Your upcoming events retrieved using" +
                            " the Google Calendar API:");
                    mEventText.setText(TextUtils.join("\n\n", events));*/ // Debugging purposes TODO delete this
                }
            }
        });
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    /**
     * Updates the 3 progress bars by calling each specified function.
     */
    public void calcFreeTime() {
        calcFreeTimeForToday();
        calcFreeTimeForThisWeek();
        calcFreeTimeForNextSevenDays();
    }

    /**
     * TODO write description and comment code
     */
    public void calcFreeTimeForToday() {
        // sets today to the start of the day
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        int totalTimeToday = NotifyUser.calculateTotalTime(today);
        Log.d("totalTime", String.valueOf(totalTimeToday));
        // calculates the assignments due between now and the end of the day
        ArrayList<Assignment> assignmentsDueToday = new ArrayList<>();
        for (Assignment assignment : usersAssignments) {
            if (assignment.getDueDate().get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                assignment.getDueDate().get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                assignmentsDueToday.add(assignment);
            }
        }
        // subtracts time in calendar events from the total time the user has today
        int freeTime = parseEventList(totalTimeToday, today);
        int freeTimeLeft =  calculateFreeTime(freeTime, assignmentsDueToday);

        // Doesn't allow the two text fields to Display less than 0/0 for progress
        if (freeTime < 0) {
            freeTime = 0;
        }
        if (freeTimeLeft < 0) {
            freeTimeLeft = 0;
        }
        int mId = 001;
        String contentTitle = "Time Left in Day";
        //System.out.print(assignmentsDueToday.get(0).getSubject());
        if (!assignmentsDueToday.isEmpty()){
            contentTitle = (assignmentsDueToday.get(0).getSubject()+ " Due " + Integer.toString(freeTime)+" Hours");
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(contentTitle)
                        .setContentText(Integer.toString(freeTimeLeft)+" Hours Free Time Left in the Day")
                        .setOngoing(true)
                        .setProgress(freeTime,freeTimeLeft,false)
                        .setWhen(System.currentTimeMillis());

        PendingIntent pendingResultIntent;
        Intent resultIntent = new Intent();
        resultIntent.setClass(this,MainActivity.class);
        pendingResultIntent = PendingIntent.getActivity(this,0,resultIntent,0);
        mBuilder.setContentIntent(pendingResultIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

        ((TextView)findViewById(R.id.textViewToday)).setText(freeTimeLeft + "/" + freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressDay)).setMax(freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressDay)).setProgress(freeTimeLeft);
    }

    /**
     * TODO write description and comment code
     */
    public void calcFreeTimeForThisWeek() {
        // sets today to the start of the day
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar endOfTheWeek = Calendar.getInstance();
        endOfTheWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        endOfTheWeek.add(Calendar.DATE, 7);

        // calculates the assignments due between now and the end of the week
        int totalTimeThisWeek = NotifyUser.calculateTotalTime(endOfTheWeek);
        ArrayList<Assignment> assignmentsDueBeforeMonday = new ArrayList<>();
        for (Assignment assignment : usersAssignments) {
            if (!assignment.getDueDate().before(today) && !assignment.getDueDate().after(endOfTheWeek)) {
                assignmentsDueBeforeMonday.add(assignment);
            }
        }
        // subtracts time in calendar events from the total time the user has this week
        int freeTime = parseEventList(totalTimeThisWeek, endOfTheWeek);
        int freeTimeLeft =  calculateFreeTime(freeTime, assignmentsDueBeforeMonday);

        // Doesn't allow the two text fields to Display less than 0/0 for progress
        if (freeTime < 0) {
            freeTime = 0;
        }
        if (freeTimeLeft < 0) {
            freeTimeLeft = 0;
        }

        ((TextView)findViewById(R.id.textViewProgressWeek)).setText(freeTimeLeft + "/" + freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressWeek)).setMax(freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressWeek)).setProgress(freeTimeLeft);
    }

    /**
     * TODO write description and comment code
     */
    public void calcFreeTimeForNextSevenDays() {
        // sets today to the start of the day
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.DATE, 6);
        // calculates the assignments due between now and 7 days from today
        int totalTimeThisWeek = NotifyUser.calculateTotalTime(nextWeek);
        ArrayList<Assignment> assignmentsDueBeforeMonday = new ArrayList<>();
        for (Assignment assignment : usersAssignments) {
            if (!assignment.getDueDate().before(today) && !assignment.getDueDate().after(nextWeek)) {
                assignmentsDueBeforeMonday.add(assignment);
            }
        }
        // subtracts time in calendar events from the total time the user has this week
        int freeTime = parseEventList(totalTimeThisWeek, nextWeek);
        int freeTimeLeft =  calculateFreeTime(freeTime, assignmentsDueBeforeMonday);

        // Doesn't allow the two text fields to Display less than 0/0 for progress
        if (freeTime < 0) {
            freeTime = 0;
        }
        if (freeTimeLeft < 0) {
            freeTimeLeft = 0;
        }

        ((TextView)findViewById(R.id.textViewProgressSevenDays)).setText(freeTimeLeft + "/" + freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressSevenDays)).setMax(freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressSevenDays)).setProgress(freeTimeLeft);

    }

    /**
     * Event class used for parsing the Google Calendar.
     * Used in finding the hours blocked out during the day
     */
    public class Event {
        private Calendar start;
        private Calendar end;
        private List<Integer> blockedOutHours = new ArrayList<>();

        public Event(Calendar start, Calendar end) {
            this.start = start;
            this.end = end;

            // Calculates the hours of day that the event blocks out
            Calendar tempTime = start;
            while(tempTime.before(end)) {
                blockedOutHours.add(tempTime.get(Calendar.HOUR_OF_DAY));
                tempTime.roll(Calendar.HOUR_OF_DAY, true);
            }

        }
        public Calendar getStart() {
            return this.start;
        }
        public Calendar getEnd() {
            return this.end;
        }
        public List<Integer> getBlockedOutHours() {
            return blockedOutHours;
        }
    }



    /**
     * TODO write description and comment code
     * @param freeTime the total number of hours between the current time and the future specified time
     * @param finalDate the future specified time an assignment may be due or the end of the week
     * @return the number of hours the user isn't busy, based on their Google Calendar events
     */
    public int parseEventList(int freeTime, Calendar finalDate)  {
        DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a z");

        int timeTakenForEvents = 0;
        List<com.google.api.services.calendar.model.Event> totalEvents = new ArrayList<>();
        try {
            totalEvents =  new EventFetchTask(this).fetchEventsFromCalendar();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(totalEvents.toString());

        for (com.google.api.services.calendar.model.Event event : totalEvents) {
            String date = event.getStart().getDate().toString();
//            Date tempDate = date.toDate();
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(sdf.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("string", sdf.format(calendar.toString()));
        }

        List<Event> eventsBeforeDeadline = new ArrayList<>();




        Calendar tempTime = Calendar.getInstance();
        tempTime.set(Calendar.HOUR_OF_DAY, 0);
        tempTime.set(Calendar.MINUTE, 0);
        tempTime.set(Calendar.SECOND, 0);
        tempTime.set(Calendar.MILLISECOND, 0);

        List<Integer> hoursOfDayBusy;

        // Checks each day for the hours that have/include an event
        // Sums up this number of hours for each day
        // ** Avoids double counting
        if (!eventsBeforeDeadline.isEmpty()) {

            while (!finalDate.before(tempTime)) {
                hoursOfDayBusy = new ArrayList<>();
                for (Event event : eventsBeforeDeadline) {
                    if (event.getStart().get(Calendar.DAY_OF_MONTH) == tempTime.get(Calendar.DAY_OF_MONTH)) {
                        // For when we are in an event
                        if (event.getStart().before(Calendar.getInstance()) && event.getEnd().after(Calendar.getInstance())) {
                            for (int i = 0; i < event.blockedOutHours.size(); i++) {
                                // Only adds hours that are after 7 AM and not already populated and is after the current time
                                if (!hoursOfDayBusy.contains(event.getBlockedOutHours().get(i))
                                        && event.getBlockedOutHours().get(i) >= 8
                                        && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= event.getBlockedOutHours().get(i)) {
                                    hoursOfDayBusy.add(event.getBlockedOutHours().get(i));
                                }
                            }
                        } else {
                            for (int i = 0; i < event.blockedOutHours.size(); i++) {
                                // Only adds hours that are after 7 AM and not already populated
                                if (!hoursOfDayBusy.contains(event.getBlockedOutHours().get(i))
                                        && event.getBlockedOutHours().get(i) >= 8) {
                                    hoursOfDayBusy.add(event.getBlockedOutHours().get(i));
                                }
                            }

                        }
                        timeTakenForEvents += hoursOfDayBusy.size();
                    }
                }

                tempTime.roll(Calendar.DAY_OF_MONTH, true);
            }
        }
        return freeTime - timeTakenForEvents;
    }
}
