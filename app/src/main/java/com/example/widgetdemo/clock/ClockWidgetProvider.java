package com.example.widgetdemo.clock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.example.widgetdemo.R;
import com.example.widgetdemo.clock.manager.ClockLogic;
import com.example.widgetdemo.clock.manager.SunArc;

// Widget trên màn hình chính
public class ClockWidgetProvider extends AppWidgetProvider {

    // xét chiều dài 4x2 mới hiện vòm
    private static final int LARGE_MIN_HEIGHT_DP = 110;
    // xét chiều rộng
    private static final int SMALL_MAX_WIDTH_DP = 150;

    // mốc chia thời gian cho vòm chạy
    private static final float MORNING_END_PROGRESS = 5f / 12f;
    private static final float NOON_END_PROGRESS = 9f / 12f;

    // cập nhật dlieu khi ra main screen
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // user gọi ra màn hình
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    // xoá múi giờ đã lưu (after user xoá)
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            ClockLogic.removeTimeZone(context, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // lấy múi giờ + kích thước htai của widget
        String timeZoneId = ClockLogic.getTimeZone(context, appWidgetId);
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        // truyền múi giờ vào TextView
        views.setString(R.id.tv_clock_time, "setTimeZone", timeZoneId);
        views.setString(R.id.tv_clock_date, "setTimeZone", timeZoneId);

        applySize(context, views, timeZoneId, minWidth, minHeight);
        applyTheme(views, SunArc.getSunProgress(timeZoneId));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    //ẩn chữ based on size widget
    private static void applySize(Context context, RemoteViews views, String timeZoneId, int minWidth, int minHeight) {
        boolean showArc = minHeight >= LARGE_MIN_HEIGHT_DP;
        float timeSize;
        float dateSize;

        // kích thước chữ dựa trên size của widget
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

    // kích thước 2x2, 2x3, 2x4 hiện vòm (do dư không gian)
        if (showArc) {
            views.setImageViewBitmap(R.id.img_solar_arc, SunArc.drawSolarArc(context, timeZoneId));
        }
    }

    // đổi nền dựa trên thời gian + vị trí cục mặt trời/trăng
    private static void applyTheme(RemoteViews views, float sunProgress) {
        int background;
        int icon = R.drawable.clock_sun;

        // phân loại buổi trong ngày dựa trên giá trị sunProgress từ 0 đến 1
        if (sunProgress <= 0f || sunProgress >= 1f) {
            background = R.drawable.clock_night; // Ban đêm
            icon = R.drawable.clock_moon;
        } else if (sunProgress < MORNING_END_PROGRESS) {
            background = R.drawable.clock_morning; // sáng
        } else if (sunProgress < NOON_END_PROGRESS) {
            background = R.drawable.clock_noon; // trưa
        } else {
            background = R.drawable.clock_sunset; // chiều / sunset
        }

        // nền + icon tương ứng
        views.setInt(R.id.clock_root_layout, "setBackgroundResource", background);
        views.setImageViewResource(R.id.img_day_night_icon, icon);
    }
}