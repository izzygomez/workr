package com.izzygomez.workr;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ismael on 4/21/2015.
 * An asynchronous task that handles the Calendar API event list retrieval.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class EventFetchTask extends AsyncTask<Void, Void, Void> {
    public MainActivity mActivity;

    /**
     * Constructor.
     * @param activity UpcomingEventsActivity that spawned this task.
     */
    EventFetchTask(MainActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Calendar API to fetch event list.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearEvents();
            mActivity.updateEventList(fetchEventsFromCalendar(15)); // <---- amount of events retrieved

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (IOException e) {
            mActivity.updateStatus("The following error occurred: " +
                    e.getMessage());
        }

        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    public List<String> fetchEventsFromCalendar(int amountOfEvents) throws IOException {
        // List the next amountOfEvents events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime aMonthFromNow = new DateTime(System.currentTimeMillis()+2628000000L);
        System.out.println(now);
        System.out.println(aMonthFromNow);
        List<String> eventStrings = new ArrayList<>();
        Events events = mActivity.mService.events().list("primary")
                .setMaxResults(amountOfEvents)
                .setTimeMin(now)
                .setTimeMax(aMonthFromNow)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start = event.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s, %s)", event.getSummary(), start, end));
        }
        return eventStrings;
    }

}
