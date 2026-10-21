package com.example.widgetdemo.calendar;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventStorage {
    private static final String PREFS_NAME = "event_widget_storage";
    private static final String KEY_EVENTS = "events";

    private EventStorage() {}

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<EventItem> loadEvents(Context context) {
        String json = preferences(context).getString(KEY_EVENTS, null);
        List<EventItem> events = new ArrayList<>();

        if (json == null) {
            long now = System.currentTimeMillis();
            long hour = 3600 * 1000L;
            long day = 24 * hour;

            events.add(new EventItem(UUID.randomUUID().toString(), "Họp nhóm dự án App Widget", now + 2 * hour));
            events.add(new EventItem(UUID.randomUUID().toString(), "Nộp báo cáo giữa kỳ", now + 1 * day));
            events.add(new EventItem(UUID.randomUUID().toString(), "Thuyết trình đồ án", now + 3 * day));
            saveEvents(context, events);
            return events;
        }

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                events.add(new EventItem(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getLong("timestamp")
                ));
            }
        } catch (JSONException e) {
            // fallback empty
        }

        return events;
    }

    public static void saveEvents(Context context, List<EventItem> events) {
        JSONArray array = new JSONArray();
        try {
            for (EventItem item : events) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                obj.put("title", item.getTitle());
                obj.put("timestamp", item.getTimestamp());
                array.put(obj);
            }
        } catch (JSONException e) {
            // ignore
        }
        preferences(context).edit().putString(KEY_EVENTS, array.toString()).apply();
    }

    public static boolean addEvent(Context context, String title, long timestamp) {
        String cleanTitle = title.trim();
        if (cleanTitle.isEmpty()) {
            return false;
        }
        List<EventItem> events = loadEvents(context);
        events.add(new EventItem(UUID.randomUUID().toString(), cleanTitle, timestamp));
        saveEvents(context, events);
        return true;
    }

    public static void deleteEvent(Context context, String id) {
        List<EventItem> events = loadEvents(context);
        events.removeIf(item -> item.getId().equals(id));
        saveEvents(context, events);
    }
}
