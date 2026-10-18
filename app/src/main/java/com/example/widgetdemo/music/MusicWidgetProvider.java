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
    private static String currArtist = "Ca sĩ";
    private static boolean hasStartedPlayback = false;
    private static int currentCoverResource = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            appWidgetManager.updateAppWidget(appWidgetId, createViews(context));
            Log.d("Musicdecor", "Khởi tạo widget");
        }
    }

    private static RemoteViews createViews(Context context){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        if (hasStartedPlayback && currentCoverResource != 0) {
            views.setImageViewResource(
                    R.id.music_cover,
                    currentCoverResource
            );

            views.setViewPadding(
                    R.id.music_cover,
                    0, 0, 0, 0
            );
        } else {
            views.setImageViewResource(
                    R.id.music_cover,
                    R.drawable.ic_music_note
            );

            int padding = Math.round(
                    8 * context.getResources().getDisplayMetrics().density
            );

            views.setViewPadding(
                    R.id.music_cover,
                    padding, padding, padding, padding
            );
        }

        views.setTextViewText(R.id.music_title, currTitle);
        views.setTextViewText(R.id.music_artist, currArtist);

        int icon = showPause ? R.drawable.ic_pause_test : R.drawable.ic_play_test;

        int description = showPause ? R.string.music_pause: R.string.music_play;

        views.setImageViewResource(R.id.music_play_pause, icon);
        views.setContentDescription(R.id.music_play_pause, context.getString(description));

        views.setOnClickPendingIntent(
                R.id.music_play_pause,
                createServiceAction(
                        context,
                        MusicPlaybackService.ACTION_TOGGLE,
                        0
                )
        );

        views.setOnClickPendingIntent(
                R.id.music_previous,
                createServiceAction(
                        context,
                        MusicPlaybackService.ACTION_PREVIOUS,
                        1
                )
        );

        views.setOnClickPendingIntent(
                R.id.music_next,
                createServiceAction(
                        context,
                        MusicPlaybackService.ACTION_NEXT,
                        2
                )
        );
        return views;
    }

    private static PendingIntent createServiceAction(Context context, String action, int requestCode){
        Intent intent = new Intent(context, MusicPlaybackService.class);
        intent.setAction(action);

        return PendingIntent.getForegroundService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
    public static void updateAllWidget(Context context, String artist, String title, boolean shouldShowPause, int coverResource){
        currTitle = title;
        currArtist = artist;
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
}
