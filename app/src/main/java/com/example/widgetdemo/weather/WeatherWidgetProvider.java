package com.example.widgetdemo.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.widgetdemo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "android.appwidget.action.ACTION_REFRESH_WEATHER";
    private static final String API_KEY = "7bbd77a9bd87869b030a8a2aa38960cc";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(context, appWidgetManager, appWidgetId);
            } else {
                ComponentName thisWidget = new ComponentName(context, WeatherWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                for (int id : appWidgetIds) {
                    updateWidget(context, appWidgetManager, id);
                }
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            CityConfigActivity.deleteCityPref(context, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        Intent refreshIntent = new Intent(context, WeatherWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntent.setData(Uri.parse("weather://widget/refresh/" + appWidgetId));

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, flags);
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingRefreshIntent);

        Intent configIntent = new Intent(context, CityConfigActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.setData(Uri.parse("weather://widget/config/" + appWidgetId));

        PendingIntent pendingConfigIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, flags);
        views.setOnClickPendingIntent(R.id.txtLocation, pendingConfigIntent);

        String city = CityConfigActivity.getSavedCity(context, appWidgetId);
        views.setTextViewText(R.id.txtLocation, city);
        views.setTextViewText(R.id.txtStatus, "Đang tải dữ liệu...");
        appWidgetManager.updateAppWidget(appWidgetId, views);

        if (!isNetworkAvailable(context)) {
            views.setTextViewText(R.id.txtCondition, "Không có kết nối Internet");
            views.setTextViewText(R.id.txtStatus, "Lỗi mạng - Vui lòng kiểm tra Wi-Fi/4G");
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        final PendingResult pendingResult = goAsync();

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String encodedCity = URLEncoder.encode(city, "UTF-8");
                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                        + encodedCity + "&appid=" + API_KEY + "&units=metric&lang=vi";

                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonObject = new JSONObject(response.toString());
                    String cityName = jsonObject.optString("name", city);

                    JSONObject main = jsonObject.getJSONObject("main");
                    long temp = Math.round(main.getDouble("temp"));

                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weatherObj = weatherArray.getJSONObject(0);
                    String icon = weatherObj.getString("icon");
                    String description = weatherObj.getString("description");
                    if (!description.isEmpty()) {
                        description = description.substring(0, 1).toUpperCase() + description.substring(1);
                    }

                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                    String imageName = "i_" + icon;
                    int resId = context.getResources().getIdentifier(
                            imageName,
                            "drawable",
                            context.getPackageName()
                    );

                    if (resId != 0) {
                        views.setImageViewResource(R.id.imgWeatherIcon, resId);
                    } else {
                        views.setImageViewResource(R.id.imgWeatherIcon, R.drawable.i_01d);
                    }

                    views.setTextViewText(R.id.txtLocation, cityName);
                    views.setTextViewText(R.id.txtTemperature, temp + "°C");
                    views.setTextViewText(R.id.txtCondition, description);
                    views.setTextViewText(R.id.txtStatus, "Cập nhật lúc " + currentTime);
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    views.setTextViewText(R.id.txtCondition, "Không tìm thấy thành phố");
                    views.setTextViewText(R.id.txtStatus, "Lỗi HTTP 404");
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    views.setTextViewText(R.id.txtCondition, "API Key không hợp lệ");
                    views.setTextViewText(R.id.txtStatus, "Lỗi HTTP 401");
                } else {
                    views.setTextViewText(R.id.txtCondition, "Lỗi máy chủ (" + responseCode + ")");
                    views.setTextViewText(R.id.txtStatus, "Vui lòng thử lại sau");
                }
            } catch (SocketTimeoutException e) {
                views.setTextViewText(R.id.txtCondition, "Hết thời gian kết nối");
                views.setTextViewText(R.id.txtStatus, "Timeout - Thử lại sau");
            } catch (UnknownHostException e) {
                views.setTextViewText(R.id.txtCondition, "Không thể kết nối máy chủ");
                views.setTextViewText(R.id.txtStatus, "Kiểm tra kết nối mạng");
            } catch (Exception e) {
                views.setTextViewText(R.id.txtCondition, "Không thể lấy dữ liệu");
                views.setTextViewText(R.id.txtStatus, "Lỗi kết nối API");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                appWidgetManager.updateAppWidget(appWidgetId, views);
                pendingResult.finish();
            }
        });
    }

    public static void updateWidgetAfterConfig(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        WeatherWidgetProvider provider = new WeatherWidgetProvider();
        provider.updateWidget(context, appWidgetManager, appWidgetId);
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }
}