package com.example.widgetdemo.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.example.widgetdemo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_PREV = "com.example.widgetdemo.calendar.ACTION_PREV";
    private static final String ACTION_NEXT = "com.example.widgetdemo.calendar.ACTION_NEXT";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (ACTION_PREV.equals(action)) {
            shiftMonth(context, appWidgetId, -1);
            updateAllWidgets(context);
        } else if (ACTION_NEXT.equals(action)) {
            shiftMonth(context, appWidgetId, 1);
            updateAllWidgets(context);
        }
    }

    private static void shiftMonth(Context context, int widgetId, int delta) {
        SharedPreferences prefs = context.getSharedPreferences("calendar_widget_state", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        int year = prefs.getInt("year_" + widgetId, cal.get(Calendar.YEAR));
        int month = prefs.getInt("month_" + widgetId, cal.get(Calendar.MONTH));

        month += delta;
        if (month > 11) {
            month = 0;
            year++;
        } else if (month < 0) {
            month = 11;
            year--;
        }

        prefs.edit()
                .putInt("year_" + widgetId, year)
                .putInt("month_" + widgetId, month)
                .apply();
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = createViews(context, appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews createViews(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.calendar_widget);

        Intent addIntent = new Intent(context, EventAddActivity.class);
        addIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        addIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addIntent.setData(Uri.parse("calendar://widget/add/" + appWidgetId));
        PendingIntent pendingAddIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_add_event_widget, pendingAddIntent);

        Intent prevIntent = new Intent(context, CalendarWidgetProvider.class);
        prevIntent.setAction(ACTION_PREV);
        prevIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        prevIntent.setData(Uri.parse("calendar://widget/prev/" + appWidgetId));
        PendingIntent pendingPrev = PendingIntent.getBroadcast(
                context,
                appWidgetId + 2000,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_prev_month, pendingPrev);

        Intent nextIntent = new Intent(context, CalendarWidgetProvider.class);
        nextIntent.setAction(ACTION_NEXT);
        nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        nextIntent.setData(Uri.parse("calendar://widget/next/" + appWidgetId));
        PendingIntent pendingNext = PendingIntent.getBroadcast(
                context,
                appWidgetId + 3000,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_next_month, pendingNext);

        SharedPreferences prefs = context.getSharedPreferences("calendar_widget_state", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        int year = prefs.getInt("year_" + appWidgetId, cal.get(Calendar.YEAR));
        int month = prefs.getInt("month_" + appWidgetId, cal.get(Calendar.MONTH));

        // Set Header Title in Vietnamese (e.g. "Tháng 4 2025")
        views.setTextViewText(R.id.tv_month_title, "Tháng " + (month + 1) + " " + year);

        // Build Calendar Grid (5 rows x 7 days = 35 cells)
        views.removeAllViews(R.id.layout_calendar_grid);

        cal.set(year, month, 1, 0, 0, 0);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1, Monday=2 ...
        int startIndex = (firstDayOfWeek - Calendar.MONDAY + 7) % 7; // Monday = 0

        List<EventItem> allEvents = EventStorage.loadEvents(context);

        int currentDayCounter = 1 - startIndex;
        for (int rowIdx = 0; rowIdx < 5; rowIdx++) {
            RemoteViews rowView = new RemoteViews(context.getPackageName(), R.layout.calendar_grid_row);

            for (int colIdx = 0; colIdx < 7; colIdx++) {
                RemoteViews cellView = new RemoteViews(context.getPackageName(), R.layout.calendar_day_cell);

                if (currentDayCounter > 0 && currentDayCounter <= maxDays) {
                    cellView.setTextViewText(R.id.tv_day_number, String.valueOf(currentDayCounter));
                    cellView.setViewVisibility(R.id.tv_day_number, View.VISIBLE);

                    // Find events for this specific day
                    Calendar dayCal = Calendar.getInstance();
                    dayCal.set(year, month, currentDayCounter, 0, 0, 0);
                    long dayStart = dayCal.getTimeInMillis();
                    dayCal.set(year, month, currentDayCounter, 23, 59, 59);
                    long dayEnd = dayCal.getTimeInMillis();

                    List<EventItem> dayEvents = new ArrayList<>();
                    for (EventItem event : allEvents) {
                        if (event.getTimestamp() >= dayStart && event.getTimestamp() <= dayEnd) {
                            dayEvents.add(event);
                        }
                    }

                    cellView.removeAllViews(R.id.layout_cell_events);
                    for (int e = 0; e < Math.min(2, dayEvents.size()); e++) {
                        EventItem ev = dayEvents.get(e);
                        RemoteViews chipView = new RemoteViews(context.getPackageName(), R.layout.calendar_event_chip);
                        chipView.setTextViewText(R.id.tv_event_chip, ev.getTitle());
                        cellView.addView(R.id.layout_cell_events, chipView);
                    }

                    // Click on day cell to view details in EventDetailActivity
                    Intent detailIntent = new Intent(context, EventDetailActivity.class);
                    detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    detailIntent.putExtra(EventDetailActivity.EXTRA_YEAR, year);
                    detailIntent.putExtra(EventDetailActivity.EXTRA_MONTH, month);
                    detailIntent.putExtra(EventDetailActivity.EXTRA_DAY, currentDayCounter);
                    detailIntent.setData(Uri.parse("calendar://widget/day/" + appWidgetId + "/" + year + "/" + month + "/" + currentDayCounter));
                    PendingIntent pendingDetail = PendingIntent.getActivity(
                            context,
                            (appWidgetId * 10000) + currentDayCounter,
                            detailIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    cellView.setOnClickPendingIntent(R.id.layout_day_cell_root, pendingDetail);

                } else {
                    cellView.setTextViewText(R.id.tv_day_number, "");
                    cellView.setViewVisibility(R.id.tv_day_number, View.INVISIBLE);
                    cellView.removeAllViews(R.id.layout_cell_events);
                }

                rowView.addView(R.id.row_container, cellView);
                currentDayCounter++;
            }

            views.addView(R.id.layout_calendar_grid, rowView);
        }

        return views;
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, CalendarWidgetProvider.class);
        int[] widgetIds = manager.getAppWidgetIds(provider);

        for (int widgetId : widgetIds) {
            updateWidget(context, manager, widgetId);
        }
    }
}
