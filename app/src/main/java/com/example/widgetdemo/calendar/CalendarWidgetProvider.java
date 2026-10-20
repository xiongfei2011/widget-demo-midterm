package com.example.widgetdemo.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.example.widgetdemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_DELETE = "com.example.widgetdemo.calendar.ACTION_DELETE";
    private static final String EXTRA_EVENT_ID = "com.example.widgetdemo.calendar.EXTRA_EVENT_ID";

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

        if (ACTION_DELETE.equals(action)) {
            String eventId = intent.getStringExtra(EXTRA_EVENT_ID);
            if (eventId != null) {
                EventStorage.deleteEvent(context, eventId);
                updateAllWidgets(context);
            }
        }
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

        long currentTime = System.currentTimeMillis();
        List<EventItem> allEvents = EventStorage.loadEvents(context);
        List<EventItem> upcomingEvents = new ArrayList<>();

        for (EventItem item : allEvents) {
            if (item.getTimestamp() >= currentTime) {
                upcomingEvents.add(item);
            }
        }

        upcomingEvents.sort(Comparator.comparingLong(EventItem::getTimestamp));

        int maxDisplay = Math.min(3, upcomingEvents.size());
        List<EventItem> topEvents = upcomingEvents.subList(0, maxDisplay);

        views.removeAllViews(R.id.layout_event_list);

        if (topEvents.isEmpty()) {
            views.setViewVisibility(R.id.tv_no_events, View.VISIBLE);
            views.setViewVisibility(R.id.layout_event_list, View.GONE);
        } else {
            views.setViewVisibility(R.id.tv_no_events, View.GONE);
            views.setViewVisibility(R.id.layout_event_list, View.VISIBLE);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            for (int i = 0; i < topEvents.size(); i++) {
                EventItem event = topEvents.get(i);
                RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.calendar_event_item);

                row.setTextViewText(R.id.tv_event_title, event.getTitle());
                row.setTextViewText(R.id.tv_event_time, sdf.format(new Date(event.getTimestamp())));

                Intent deleteIntent = new Intent(context, CalendarWidgetProvider.class);
                deleteIntent.setAction(ACTION_DELETE);
                deleteIntent.putExtra(EXTRA_EVENT_ID, event.getId());
                deleteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                deleteIntent.setData(Uri.parse("calendar://widget/delete/" + appWidgetId + "/" + event.getId()));
                PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(
                        context,
                        (appWidgetId * 100) + i,
                        deleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                row.setOnClickPendingIntent(R.id.btn_delete_event, pendingDeleteIntent);

                views.addView(R.id.layout_event_list, row);
            }
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
