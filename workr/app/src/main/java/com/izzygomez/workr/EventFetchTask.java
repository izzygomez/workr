package com.izzygomez.workr;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
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
            mActivity.updateEventList(fetchEventsFromCalendar());

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
     * Fetch a list of the events from all selected calendars in the next month.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    public List<String> fetchEventsFromCalendar() throws IOException {
        // Define what "now" and "month from now" are
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime aMonthFromNow = new DateTime(System.currentTimeMillis()+2628000000L);

        // init empty arrays for Event objects and corresponding strings (that we format later)
        List<Event> allEvents = new ArrayList<>();
        List<String> eventStrings = new ArrayList<>();

        // Find all calendars from user that are NOT deleted and NOT hidden
        CalendarList calendarList = mActivity.mService.calendarList().list().execute();
        List<CalendarListEntry> allCalendars = calendarList.getItems();

        for (CalendarListEntry calendarListEntry : allCalendars) {
            // If the calendar is *selected* (i.e. shows up in the GCal UI for user)
            if (calendarListEntry.isSelected()) {
                // Extract events between now and a month from now...
                Events events = mActivity.mService.events().list(calendarListEntry.getId())
                        .setTimeMin(now)
                        .setTimeMax(aMonthFromNow)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                // ...and add them to allEvents
                allEvents.addAll(events.getItems());
            }
        }

        for (Event event : allEvents) {
            // Get start and end times...
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();

            // DON'T include all-day events
            if (start == null) continue;

            // ...and add to eventStrings
            eventStrings.add(
                    String.format("%s (%s, %s)", event.getSummary(), start, end));
        }

        return eventStrings;
    }

}
