package com.example.widgetdemo.calendar;

public class EventItem {
    private final String id;
    private final String title;
    private final long timestamp; // milliseconds

    public EventItem(String id, String title, long timestamp) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
