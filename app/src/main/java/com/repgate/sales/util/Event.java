package com.repgate.sales.util;

import com.github.gfranks.collapsible.calendar.model.CollapsibleCalendarEvent;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Created by developer on 12/29/2016.
 */

public class Event extends CollapsibleCalendarEvent {
    private String mTitle;
    private String mSender;
    private String mReceiver;
    private String mDateTime;
    private String mEventId;
    private long mDate;
    private String mStatus;

    public Event(String title, String sender, String receiver, String strDate, String eventId, String status, long date) {
        mTitle = title;
        mSender = sender;
        mReceiver = receiver;
        mDate = date;
        mDateTime = strDate;
        mEventId = eventId;
        mStatus = status;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSender() {
        return mSender;
    }

    public String getReceiver() {
        return mReceiver;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public String getEventId() {
        return mEventId;
    }

    public DateTime getListCellTime() {
        return new DateTime(mDate);
    }

    @Override
    public LocalDate getCollapsibleEventLocalDate() {
        return new LocalDate(mDate);
    }
}
