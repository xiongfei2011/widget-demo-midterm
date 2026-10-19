package com.example.widgetdemo.todo;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.widgetdemo.R;

import java.util.Collections;
import java.util.List;

public class TodoWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = createViews(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    private static RemoteViews createViews(Context context){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget);

        bindAddButton(context, views);

        List<TodoItem> items;
        String emptyMessage = context.getString(R.string.todo_empty_message);

        try {
            items = TodoStorage.loadItem(context);
        } catch (IllegalStateException e ) {
            Toast.makeText(context, "Cannot load tasks", Toast.LENGTH_SHORT).show();

            items = Collections.emptyList();
            emptyMessage = context.getString(R.string.todo_error_storage);
        }

        bindTaskList(context, views, items);

        views.setTextViewText(R.id.tv_empty, emptyMessage);
        views.setEmptyView(R.id.list_tasks, R.id.tv_empty);

        views.setTextViewText(R.id.tv_task_count, context.getString(R.string.todo_task_count, 0, items.size()));
        views.setProgressBar(R.id.progress_tasks, 100, 0, false);

        return views;
    }

    private static void bindAddButton(Context context, RemoteViews views){
        Intent intent = new Intent(context, TodoAddActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.btn_add_task, pendingIntent);
    }

    private static void bindTaskList(Context context, RemoteViews views, List<TodoItem> items){
        RemoteViews.RemoteCollectionItems.Builder collection =
                new RemoteViews.RemoteCollectionItems.Builder()
                        .setViewTypeCount(1)
                        .setHasStableIds(false);

        for (int i = 0; i < items.size(); i++){
            TodoItem item = items.get(i);

            RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.todo_task_item);

            row.setTextViewText(R.id.tv_task_title, item.getTitle());

            row.setViewVisibility(R.id.tv_task_meta, View.GONE);
            row.setViewVisibility(R.id.task_section_divider, View.GONE);

            row.setInt(R.id.img_task_check,
                    "setBackgroundResource",
                    R.drawable.todo_unchecked_bg
                    );

            row.setImageViewResource(R.id.img_task_check, 0);

            collection.addItem(i,row);
        }

        views.setRemoteAdapter(R.id.list_tasks, collection.build());

    }
}
