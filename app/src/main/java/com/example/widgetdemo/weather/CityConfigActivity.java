package com.example.widgetdemo.weather;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.widgetdemo.R;

public class CityConfigActivity extends AppCompatActivity {

    private Spinner spinnerCities;
    private Button btnSaveCity;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static final String PREFS_NAME_PREFIX = "WeatherWidgetPrefs_";
    private static final String KEY_CITY_NAME = "city_name";

    private final String[] cityDisplayNames = {
            "Hồ Chí Minh",
            "Hà Nội",
            "Đà Nẵng",
            "Cần Thơ",
            "Hải Phòng",
            "Nha Trang",
            "Huế",
            "Đà Lạt",
            "Tokyo",
            "London",
            "New York"
    };

    private final String[] cityApiQueries = {
            "Ho Chi Minh",
            "Hanoi",
            "Da Nang",
            "Can Tho",
            "Hai Phong",
            "Nha Trang",
            "Hue",
            "Da Lat",
            "Tokyo",
            "London",
            "New York"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_config);

        setResult(RESULT_CANCELED);

        spinnerCities = findViewById(R.id.spinnerCities);
        btnSaveCity = findViewById(R.id.btnSaveCity);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                cityDisplayNames
        );
        spinnerCities.setAdapter(adapter);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        String currentCity = getSavedCity(this, appWidgetId);
        for (int i = 0; i < cityApiQueries.length; i++) {
            if (cityApiQueries[i].equalsIgnoreCase(currentCity)) {
                spinnerCities.setSelection(i);
                break;
            }
        }

        btnSaveCity.setOnClickListener(v -> {
            int selectedPosition = spinnerCities.getSelectedItemPosition();
            String apiQuery = cityApiQueries[selectedPosition];

            saveCityPref(this, appWidgetId, apiQuery);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            WeatherWidgetProvider.updateWidgetAfterConfig(this, appWidgetManager, appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });
    }

    public static SharedPreferences getContextPreferences(Context context, int appWidgetId) {
        return context.getSharedPreferences(PREFS_NAME_PREFIX + appWidgetId, Context.MODE_PRIVATE);
    }

    public static void saveCityPref(Context context, int appWidgetId, String cityName) {
        SharedPreferences prefs = getContextPreferences(context, appWidgetId);
        prefs.edit().putString(KEY_CITY_NAME, cityName).apply();
    }

    public static String getSavedCity(Context context, int appWidgetId) {
        SharedPreferences prefs = getContextPreferences(context, appWidgetId);
        return prefs.getString(KEY_CITY_NAME, "Ho Chi Minh");
    }

    public static void deleteCityPref(Context context, int appWidgetId) {
        SharedPreferences prefs = getContextPreferences(context, appWidgetId);
        prefs.edit().clear().apply();
    }
}