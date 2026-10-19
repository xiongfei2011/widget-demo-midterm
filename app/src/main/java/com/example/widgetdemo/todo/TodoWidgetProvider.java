package com.example.widgetdemo.todo;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.widgetdemo.R;

import java.util.Collections;
import java.util.List;

public class TodoWidgetProvider extends AppWidgetProvider {

    private static final String EXTRA_TASK_ID =
            "com.example.widgetdemo.todo.EXTRA_TASK_ID";

    private static final String ACTION_TASK_ACTION =
            "com.example.widgetdemo.todo.ACTION_TASK_ACTION";

    private static final String EXTRA_OPERATION = "todo_operation";
    private static final String OP_TOGGLE = "toggle";
    private static final String OP_DELETE = "delete";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = createViews(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (!ACTION_TASK_ACTION.equals(intent.getAction())) return;

        String taskID = intent.getStringExtra(EXTRA_TASK_ID);
        String operation = intent.getStringExtra(EXTRA_OPERATION);

        if (taskID == null) return;

        try {
            if (OP_DELETE.equals(operation)) {
                TodoStorage.deleteItem(context, taskID);
            } else if (OP_TOGGLE.equals(operation)) {
                TodoItem item = TodoStorage.findItem(context, taskID);

                if (item != null) {
                    TodoStorage.updateItem(
                            context,
                            item.getId(),
                            item.getTitle(),
                            !item.isCompleted()
                    );
                }

            } else {
                return;
            }
            updateAllWidgets(context);
        } catch (IllegalStateException e) {
            Toast.makeText(
                    context,
                    R.string.todo_error_storage,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private static RemoteViews createViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget);

        bindAddButton(context, views);

        List<TodoItem> items;
        String emptyMessage = context.getString(R.string.todo_empty_message);

        try {
            items = TodoStorage.loadItem(context);
        } catch (IllegalStateException e) {
            Toast.makeText(context, "Cannot load tasks", Toast.LENGTH_SHORT).show();

            items = Collections.emptyList();
            emptyMessage = context.getString(R.string.todo_error_storage);
        }

        bindTaskList(context, views, items);

        views.setTextViewText(R.id.tv_empty, emptyMessage);
        views.setEmptyView(R.id.list_tasks, R.id.tv_empty);

        return views;
    }

    private static void bindAddButton(Context context, RemoteViews views) {
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

    private static void bindTaskList(Context context, RemoteViews views, List<TodoItem> items) {

        Intent actionIntent = new Intent(context, TodoWidgetProvider.class);
        actionIntent.setAction(ACTION_TASK_ACTION);

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_MUTABLE
        );

        views.setPendingIntentTemplate(
                R.id.list_tasks,
                actionPendingIntent
        );

        RemoteViews.RemoteCollectionItems.Builder collection =
                new RemoteViews.RemoteCollectionItems.Builder()
                        .setViewTypeCount(1) // Khai báo số lượng layout khác nhau được sử dụng
                        .setHasStableIds(false); // Xem Id có cố định hay không

        for (int i = 0; i < items.size(); i++) {
            TodoItem item = items.get(i);

            RemoteViews row = createTaskRow(context, item);

            collection.addItem(i, row);
        }

        views.setRemoteAdapter(R.id.list_tasks, collection.build());
    }

    private static RemoteViews createTaskRow(Context context, TodoItem item) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.todo_task_item);

        row.setTextViewText(R.id.tv_task_title, item.getTitle());

        boolean completed = item.isCompleted();

        row.setInt(R.id.img_task_check,
                "setBackgroundResource",
                completed ? R.drawable.todo_checked_bg : R.drawable.todo_unchecked_bg
        );

        row.setImageViewResource(R.id.img_task_check,
                completed ? R.drawable.ic_todo_check : 0);

        // Xử lý nút checked
        Intent toggleIntent = new Intent();
        toggleIntent.putExtra(EXTRA_TASK_ID, item.getId());
        toggleIntent.putExtra(EXTRA_OPERATION, OP_TOGGLE);

        row.setOnClickFillInIntent(R.id.btn_toggle_task, toggleIntent);

        // Xử lý xóa task
        Intent deleteIntent = new Intent();
        deleteIntent.putExtra(EXTRA_TASK_ID, item.getId());
        deleteIntent.putExtra(EXTRA_OPERATION, OP_DELETE);

        row.setOnClickFillInIntent(R.id.btn_delete_task, deleteIntent);

        return row;
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        ComponentName provider = new ComponentName(context, TodoWidgetProvider.class);

        int[] widgetIds = manager.getAppWidgetIds(provider);

        if (widgetIds.length == 0) {
            return;
        }

        RemoteViews views = createViews(context);
        manager.updateAppWidget(widgetIds, views);
    }
}
