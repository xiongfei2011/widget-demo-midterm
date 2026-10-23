package com.example.widgetdemo.clock.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class ClockLogic {
    //cấu hình widget
    private static final String PREF_NAME = "ClockWidgetPref";
    private static final String KEY_TIMEZONE_PREFIX = "timezone_id_";
    //mặc định là vn nếu chưa chọn
    private static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveTimeZone(Context context, int appWidgetId, String timeZoneId) {
        prefs(context).edit().putString(KEY_TIMEZONE_PREFIX + appWidgetId, timeZoneId).apply();
    }

    public static String getTimeZone(Context context, int appWidgetId) {
        return prefs(context).getString(KEY_TIMEZONE_PREFIX + appWidgetId, DEFAULT_TIMEZONE);
    }

    public static void removeTimeZone(Context context, int appWidgetId) {
        prefs(context).edit().remove(KEY_TIMEZONE_PREFIX + appWidgetId).apply();
    }
}