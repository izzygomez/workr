package com.example.calendarquickstart;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;

// CalendarList Testing
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
// </Testing>

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Calendar API event list retrieval.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class EventFetchTask extends AsyncTask<Void, Void, Void> {
    private UpcomingEventsActivity mActivity;

    /**
     * Constructor.
     * @param activity UpcomingEventsActivity that spawned this task.
     */
    EventFetchTask(UpcomingEventsActivity activity) {
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
                    UpcomingEventsActivity.REQUEST_AUTHORIZATION);

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
    private List<String> fetchEventsFromCalendar() throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime aWeekFromNow = new DateTime(System.currentTimeMillis() + 604800000L);
        List<Event> allEvents = new ArrayList<>();
        List<String> eventStrings = new ArrayList<>();
        //String pageToken = null;

        // Find all Selected calendars (i.e. calendars that show up in GCal UI) from user
        CalendarList calendarList = mActivity.mService.calendarList().list()
                //setPageToken(pageToken).
                .execute();
        List<CalendarListEntry> allCalendars = calendarList.getItems();

        for (CalendarListEntry calendarListEntry : allCalendars) {
            if (calendarListEntry.isSelected()) {
                Events events = mActivity.mService.events().list(calendarListEntry.getId())
                        .setMaxResults(10)
                        .setTimeMin(now)
                        .setTimeMax(aWeekFromNow)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                allEvents.addAll(events.getItems());
            }
        }
        //pageToken = calendarList.getNextPageToken();

       /* Events events = mActivity.mService.events().list("primary") // Usage: list(***calendarId***)
                .setMaxResults(10)
                .setTimeMin(now)
                .setTimeMax(aWeekFromNow)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        allEvents.addAll(events.getItems());*/

        for (Event event : allEvents) {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                continue;
                //start = event.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s)", event.getSummary(), start));
        }

        // CalendarListEntry Testing
//        String pageToken = null;
//        do {
//            CalendarList calendarList = mActivity.mService.calendarList().list().
//                    setPageToken(pageToken).
//                    execute();
//            List<CalendarListEntry> allCalendars = calendarList.getItems();
//
//            for (CalendarListEntry calendarListEntry : allCalendars) {
//                if (calendarListEntry.isSelected()) {
//                    //System.out.println(calendarListEntry.getSummary());
//                    //System.out.println(calendarListEntry.getId());
//
//                }
//            }
//            pageToken = calendarList.getNextPageToken();
//        } while (pageToken != null);
        // </Testing>

        return eventStrings;
    }

}