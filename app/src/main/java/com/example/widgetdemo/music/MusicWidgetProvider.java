package com.example.widgetdemo.music;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;

import com.example.widgetdemo.R;

public class MusicWidgetProvider extends AppWidgetProvider {
    // Trạng thái dùng chung cho các widget trong tiến trình hiện tại.
    private static boolean showPause = false;
    private static String currentTitle;
    private static String currentArtist;
    private static boolean hasStartedPlayback = false;
    @DrawableRes
    private static int currentCoverResource = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, createViews(context));
            Log.d("Musicdecor", "Khởi tạo widget");
        }
    }

    public static void updateAllWidgets(Context context, String artist, String title,
                                        boolean shouldShowPause, @DrawableRes int coverResource) {
        currentTitle = title;
        currentArtist = artist;
        showPause = shouldShowPause;
        currentCoverResource = coverResource;

        if (coverResource == 0) {
            hasStartedPlayback = false;
        } else if (shouldShowPause) {
            hasStartedPlayback = true;
        }

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, MusicWidgetProvider.class);
        manager.updateAppWidget(provider, createViews(context));
    }

    public static void resetWidgets(Context context) {
        updateAllWidgets(
                context,
                context.getString(R.string.music_name_artist),
                context.getString(R.string.music_default_title),
                false,
                0
        );
    }

    private static RemoteViews createViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        views.setTextViewText(R.id.music_title, currentTitle != null
                ? currentTitle : context.getString(R.string.music_default_title));
        views.setTextViewText(R.id.music_artist, currentArtist != null
                ? currentArtist : context.getString(R.string.music_name_artist));

        updateCover(context, views);
        bindControls(context, views);
        return views;
    }

    private static void updateCover(Context context, RemoteViews views) {
        boolean showCover = hasStartedPlayback && currentCoverResource != 0;
        views.setImageViewResource(R.id.music_cover,
                showCover ? currentCoverResource : R.drawable.ic_music_note);

        int padding = showCover ? 0
                : Math.round(8 * context.getResources().getDisplayMetrics().density);
        views.setViewPadding(R.id.music_cover, padding, padding, padding, padding);
    }

    private static void bindControls(Context context, RemoteViews views) {
        int icon = showPause ? R.drawable.ic_pause_test : R.drawable.ic_play_test;
        int description = showPause ? R.string.music_pause : R.string.music_play;
        views.setImageViewResource(R.id.music_play_pause, icon);
        views.setContentDescription(R.id.music_play_pause, context.getString(description));

        views.setOnClickPendingIntent(R.id.music_play_pause,
                createServiceAction(context, MusicPlaybackService.ACTION_TOGGLE, 0));
        views.setOnClickPendingIntent(R.id.music_previous,
                createServiceAction(context, MusicPlaybackService.ACTION_PREVIOUS, 1));
        views.setOnClickPendingIntent(R.id.music_next,
                createServiceAction(context, MusicPlaybackService.ACTION_NEXT, 2));
    }

    private static PendingIntent createServiceAction(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, MusicPlaybackService.class);
        intent.setAction(action);

        return PendingIntent.getForegroundService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
