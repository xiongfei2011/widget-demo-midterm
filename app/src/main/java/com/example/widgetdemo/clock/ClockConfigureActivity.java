package com.example.widgetdemo.clock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.widgetdemo.R;
import com.example.widgetdemo.clock.manager.ClockLogic;

public class ClockConfigureActivity extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.clock_configure);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // chọn múi giờ
        Button btnVietnam = findViewById(R.id.btn_vietnam);
        Button btnTokyo = findViewById(R.id.btn_tokyo);
        Button btnNewYork = findViewById(R.id.btn_newyork);

        btnVietnam.setOnClickListener(v -> saveAndFinish("Asia/Ho_Chi_Minh"));
        btnTokyo.setOnClickListener(v -> saveAndFinish("Asia/Tokyo"));
        btnNewYork.setOnClickListener(v -> saveAndFinish("America/New_York"));
    }

    private void saveAndFinish(String timeZoneId) {
        ClockLogic.saveTimeZone(this, appWidgetId, timeZoneId);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ClockWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}