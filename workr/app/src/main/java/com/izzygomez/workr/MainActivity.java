package com.izzygomez.workr;

import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.izzygomez.workr.NotifyUser.calculateFreeTime;

public class MainActivity extends ActionBarActivity {
    ArrayList<ListedItem> listItems = new ArrayList<ListedItem>();
    ArrayAdapter<ListedItem> adapter;
    boolean deleteMode = false;
//    float oldX = Float.NaN;
//    float oldY = Float.NaN;
//    static final int DELTA = 50;
//    enum Direction{LEFT, RIGHT;}
    int lastClickedRow = 10000;
    ArrayList<String> lastClickedRowArray = new ArrayList<String>(); //Array of Strings to pass to TaskInputScreen to preload input boxes during edits
    ArrayList<String> taskInputData = new ArrayList<String>(); //data that comes from the TaskInputScreen to be displayed
    ArrayList<Assignment> usersAssignments = new ArrayList<Assignment>();//title of each assignment
    List<String> usersEvents = new ArrayList<>();
    ArrayList<Integer> selectedItemsList = new ArrayList<Integer>();
    public View row;

    // <Izzy's variables>
    /**
     * A Calendar service object used to query or modify calendars via the
     * Calendar API. Note: Do not confuse this class with the
     * com.google.api.services.calendar.model.Calendar class.
     */
    com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;

    private TextView mStatusText;
    private TextView mEventText;

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

        mStatusText = (TextView) findViewById(R.id.mStatusText);
        mEventText = (TextView) findViewById(R.id.mEventText);
        ListView taskListView = (ListView) findViewById(R.id.listViewOfTasks);
        adapter = new ArrayAdapter<ListedItem>(this, android.R.layout.simple_list_item_1, listItems);

        adapter.notifyDataSetChanged();
        Log.d("listItems",listItems.toString());
//        Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG).show();
        taskListView.setAdapter(adapter);

        // PHILLY'S CODE
        calcFreeTime();

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Integer position1 = position;
                row = arg1;
                if (row != null){
                    if (!selectedItemsList.contains(position)){
                        selectedItemsList.add(position);
                        row.setBackgroundResource(R.color.wallet_holo_blue_light);
                    }
                    else{
                        row.setBackgroundResource(0);
                    }
                }


//                v.setBackgroundResource(R)
                lastClickedRow = position;
                String[] lastClickedArrayString = arg0.getItemAtPosition(position).toString().split(" ");
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
        if(!(listItems.contains(listViewInput))) {
            listItems.add(listViewInput);
            adapter.notifyDataSetChanged();
            lastClickedRowArray = taskInputs;
        }
    }

    public void clickedPlus(View v){
        lastClickedRowArray = new ArrayList<String>();
        goToTaskInputScreen();
//        List<String> taskInputs = new ArrayList<String>();
//        taskInputs.add("This");
//        taskInputs.add("is");
//        taskInputs.add("a");
//        taskInputs.add("test");
//        addToList(taskInputs);
    }

    public void addToListTest(View v){
        ArrayList<String> taskInputs = new ArrayList<String>();
        taskInputs.add("This");
        taskInputs.add("is");
        taskInputs.add("a");
        taskInputs.add("test");
        addToList(taskInputs);
    }

    public void setDeleteMode(View v){
        if (deleteMode){
            deleteMode = false;
        }
        else{
            deleteMode = true;
        }
        Toast.makeText(this, Integer.toString(lastClickedRow), Toast.LENGTH_LONG).show();
    }

    public void removeLastClickedRow(){
        if (lastClickedRow != 10000){
            listItems.remove(lastClickedRow);
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteSelectedItems(View v){
        for (int i : selectedItemsList){
            listItems.remove(i);
            Toast.makeText(this, Integer.toString(i), Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this, listItems.toString(), Toast.LENGTH_LONG).show();
        adapter.notifyDataSetChanged();
        Toast.makeText(this,selectedItemsList.toString(),Toast.LENGTH_LONG).show();
        selectedItemsList = new ArrayList<Integer>();
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
            mStatusText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
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
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy"); // or mm/dd/yy or assume 2015?

        if ((requestCode == 5) &&
                (resultCode == RESULT_OK)) {

            taskInputData = data.getExtras().getStringArrayList("returnData");
            Log.d("taskInputData", taskInputData.toString());
//            listItems.remove(lastClickedRow);
//            adapter.notifyDataSetChanged();
            try {
                cal.setTime(sdf.parse(taskInputData.get(2)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Boolean priority;
            if (taskInputData.get(3) == "High" || taskInputData.get(3) == "high") {
                priority = true;
            } else {
                priority = false;
            }
            Assignment newAssignment = new Assignment(taskInputData.get(0), Integer.parseInt(taskInputData.get(1)),
                    cal , priority);
            Log.d("newAssignment", newAssignment.toString());

            lastClickedRowArray = new ArrayList<String>();
            addToList(taskInputData);
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
                    mStatusText.setText("Account unspecified.");
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
                mStatusText.setText("No network connection available.");
            }
        }
    }

    /**
     * Clear any existing events from the list display and update the header
     * message; called from background threads and async tasks that need to
     * update the UI (in the UI thread).
     */
    public void clearEvents() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText("Retrieving events…");
                mEventText.setText("");
            }
        });
    }

    /**
     * Fill the event display with the given List of strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     * @param eventStrings a List of Strings to populate the event display with.
     */
    public void updateEventList(final List<String> eventStrings) {
        usersEvents = eventStrings;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (eventStrings == null) {
                    mStatusText.setText("Error retrieving events!");
                } else if (eventStrings.size() == 0) {
                    mStatusText.setText("No upcoming events found.");
                } else {
                    mStatusText.setText("Your upcoming events retrieved using" +
                            " the Google Calendar API:");
                    mEventText.setText(TextUtils.join("\n\n", eventStrings));
                }
            }
        });
    }

    /**
     * Show a status message in the list header TextView; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     * @param message a String to display in the UI header TextView.
     */
    public void updateStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatusText.setText(message);
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

    public void calcFreeTime() {
        Calendar endOfTheWeek = Calendar.getInstance();
        endOfTheWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        endOfTheWeek.add(Calendar.DATE,7);
        int totalTimeThisWeek = NotifyUser.calculateTotalTime(endOfTheWeek);

        ArrayList<Assignment> assignmentsDueBeforeMonday = new ArrayList<>();
        for (Assignment assignment : (ArrayList<Assignment>)usersAssignments) {
            if (!assignment.getDueDate().after(endOfTheWeek) && !Calendar.getInstance().after(assignment.getDueDate())) {
                assignmentsDueBeforeMonday.add(assignment);
            }
        }


        int freeTime = parseEventList(totalTimeThisWeek);
//        Log.d("FreeTime", String.valueOf(freeTime));
        int freeTimeLeft =  calculateFreeTime(freeTime, assignmentsDueBeforeMonday);
        Log.d("TryAgain", String.valueOf(freeTime));


        ((TextView)findViewById(R.id.textViewProgress)).setText(freeTimeLeft + "/" + freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressBar)).setMax(freeTime);
        ((ProgressBar)findViewById(R.id.freeTimeProgressBar)).setProgress(freeTimeLeft);
    }

    public int parseEventList(int freeTime)  {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        SimpleDateFormat cal = new SimpleDateFormat("yyyy-MM-dd");
        Calendar tempCal = Calendar.getInstance();
        int timeTakenForEvents = 0;

        if (usersEvents.size() > 0) {

            Log.d("events", usersEvents.toString());
            for( String event : usersEvents) {
                Calendar tempStart = Calendar.getInstance();
                Calendar tempEnd = Calendar.getInstance();

                String tempEvent = event.substring(event.indexOf("(")+1,event.lastIndexOf(")")-1);
                String start = tempEvent.substring(0, tempEvent.indexOf(","));
                start = start.substring(0, start.lastIndexOf("."));
                start = start.replace("T", " ");
                String end = tempEvent.substring(tempEvent.lastIndexOf(",") + 1, tempEvent.lastIndexOf("."));
                end = end.replace("T", " ");

                try{

                    Date startDate =  sdf.parse(start);
                    tempStart.setTime(startDate);

                    Date endDate = sdf.parse(end);
                    tempEnd.setTime(endDate);

                    if (!Calendar.getInstance().after(startDate)) {
                        if (tempEnd.get(Calendar.DAY_OF_MONTH) == tempStart.get(Calendar.DAY_OF_MONTH)) {
                            timeTakenForEvents += tempEnd.get(Calendar.HOUR) - tempStart.get(Calendar.HOUR);
                        }
                        Log.d("timeSpent", String.valueOf(tempEnd.get(Calendar.HOUR) - tempStart.get(Calendar.HOUR)) );
                    }
                }  catch (ParseException pe) {
                    pe.printStackTrace();
                }

            }



        }

        Log.d("timeTaken", String.valueOf(timeTakenForEvents));

        return freeTime - timeTakenForEvents;
    }
}
