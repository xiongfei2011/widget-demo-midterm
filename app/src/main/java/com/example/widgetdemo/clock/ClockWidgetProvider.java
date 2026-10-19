package com.example.widgetdemo.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.example.widgetdemo.R;
import com.example.widgetdemo.clock.manager.ClockLogic;
import com.example.widgetdemo.clock.manager.SunArc;

public class ClockWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.example.widgetdemo.clock.ACTION_REFRESH";

    private static final int LARGE_MIN_HEIGHT_DP = 110;
    private static final int SMALL_MAX_WIDTH_DP = 150;
    private static final int REFRESH_REQUEST_CODE = 1001;

    private static final float MORNING_END_PROGRESS = 5f / 12f;
    private static final float NOON_END_PROGRESS = 9f / 12f;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)) {
            refreshAll(context);
            scheduleRefresh(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleRefresh(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelRefresh(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleRefresh(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            ClockLogic.removeTimeZone(context, appWidgetId);
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, ClockWidgetProvider.class));
        for (int id : ids) {
            updateAppWidget(context, appWidgetManager, id);
        }
    }

    static void scheduleRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                refreshIntent(context));
    }

    static void cancelRefresh(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(refreshIntent(context));
        }
    }

    private static PendingIntent refreshIntent(Context context) {
        Intent intent = new Intent(context, ClockWidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(
                context,
                REFRESH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String timeZoneId = ClockLogic.getTimeZone(context, appWidgetId);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        views.setString(R.id.tv_clock_time, "setTimeZone", timeZoneId);
        views.setString(R.id.tv_clock_date, "setTimeZone", timeZoneId);

        applySize(context, views, timeZoneId, minWidth, minHeight);
        applyTheme(views, SunArc.getSunProgress(timeZoneId));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void applySize(Context context, RemoteViews views, String timeZoneId, int minWidth, int minHeight) {
        boolean showArc = minHeight >= LARGE_MIN_HEIGHT_DP;
        float timeSize;
        float dateSize;

        if (showArc) {
            timeSize = 32;
            dateSize = 13;
        } else if (minWidth <= SMALL_MAX_WIDTH_DP) {
            timeSize = 20;
            dateSize = 10;
        } else {
            timeSize = 24;
            dateSize = 11;
        }

        views.setTextViewTextSize(R.id.tv_clock_time, TypedValue.COMPLEX_UNIT_SP, timeSize);
        views.setTextViewTextSize(R.id.tv_clock_date, TypedValue.COMPLEX_UNIT_SP, dateSize);
        views.setViewVisibility(R.id.tv_clock_date, View.VISIBLE);
        views.setViewVisibility(R.id.img_solar_arc, showArc ? View.VISIBLE : View.GONE);

        if (showArc) {
            views.setImageViewBitmap(R.id.img_solar_arc, SunArc.drawSolarArc(context, timeZoneId));
        }
    }

    private static void applyTheme(RemoteViews views, float sunProgress) {
        int background;
        int icon = R.drawable.clock_sun;

        if (sunProgress <= 0f || sunProgress >= 1f) {
            background = R.drawable.clock_night;
            icon = R.drawable.clock_moon;
        } else if (sunProgress < MORNING_END_PROGRESS) {
            background = R.drawable.clock_morning;
        } else if (sunProgress < NOON_END_PROGRESS) {
            background = R.drawable.clock_noon;
        } else {
            background = R.drawable.clock_sunset;
        }

        views.setImageViewResource(R.id.img_clock_bg, background);
        views.setImageViewResource(R.id.img_day_night_icon, icon);
    }
}