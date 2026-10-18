package com.example.widgetdemo.clock.model;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockData {
    private final String timeZoneId;
    private final int dayOfYear;
    private final int hour;
    private final int minute;
    private final int utcOffsetMinutes;

    public ClockData(String timeZoneId) {
        this.timeZoneId = (timeZoneId != null && !timeZoneId.isEmpty())
                ? timeZoneId
                : TimeZone.getDefault().getID();
        TimeZone timeZone = TimeZone.getTimeZone(this.timeZoneId);
        Calendar calendar = Calendar.getInstance(timeZone);
        this.dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.utcOffsetMinutes = timeZone.getOffset(calendar.getTimeInMillis()) / 60000;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getMinutesOfDay() {
        return hour * 60 + minute;
    }

    public int getUtcOffsetMinutes() {
        return utcOffsetMinutes;
    }
}