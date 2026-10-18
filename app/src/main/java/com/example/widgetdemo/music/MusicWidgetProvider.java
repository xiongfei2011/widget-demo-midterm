package com.example.widgetdemo.music;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.view.KeyEventDispatcher;

import com.example.widgetdemo.R;

public class MusicWidgetProvider extends AppWidgetProvider {
    private static boolean showPause = false;
    private static String currTitle = "Chưa phát nhạc";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            appWidgetManager.updateAppWidget(appWidgetId, createViews(context));
            Log.d("Musicdecor", "Khởi tạo widget");
        }
    }

    private static RemoteViews createViews(Context context){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        views.setTextViewText(R.id.music_title, currTitle);

        int icon = showPause ? R.drawable.ic_music_pause : R.drawable.ic_music_play;

        int description = showPause ? R.string.music_pause: R.string.music_play;

        views.setImageViewResource(R.id.music_play_pause, icon);
        views.setContentDescription(R.id.music_play_pause, context.getString(description));

        Intent intent = new Intent(context, MusicPlaybackService.class);
        intent.setAction(MusicPlaybackService.ACTION_TOGGLE);

        PendingIntent pendingIntent =
                PendingIntent.getForegroundService(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
        views.setOnClickPendingIntent(R.id.music_play_pause, pendingIntent);

        return views;
    }

    public static void updateAllWidget(Context context, String title, boolean shouldShowPause){
        currTitle = title;
        showPause = shouldShowPause;

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        ComponentName provider = new ComponentName(context, MusicWidgetProvider.class);

        manager.updateAppWidget(provider, createViews(context));
    }
}
