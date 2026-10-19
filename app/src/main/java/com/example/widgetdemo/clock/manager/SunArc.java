package com.example.widgetdemo.clock.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.format.DateFormat;

import com.example.widgetdemo.clock.model.ClockData;

import java.util.Locale;

public class SunArc {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 260;

    private static final float START_X = 80f;
    private static final float START_Y = 160f;
    private static final float CONTROL_X = 400f;
    private static final float CONTROL_Y = 20f;
    private static final float END_X = 720f;
    private static final float END_Y = 160f;

    private static final float SUNSET_POSITION = 0.8f;
    private static final int END_OF_DAY_MINUTES = 24 * 60 - 1;

    private static final double SUNRISE_ZENITH_DEGREES = 90.833;

    private static double[] getCoordinates(String timeZoneId) {
        if (timeZoneId.contains("New_York")) return new double[]{40.71, -74.01};
        if (timeZoneId.contains("London")) return new double[]{51.51, -0.13};
        if (timeZoneId.contains("Tokyo")) return new double[]{35.68, 139.69};
        return new double[]{10.76, 106.66};
    }

    private static int[] calculateSunriseSunsetMinutes(ClockData clock) {
        double[] coordinates = getCoordinates(clock.getTimeZoneId());
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        double gamma = 2.0 * Math.PI / 365.0 * (clock.getDayOfYear() - 1);

        double equationOfTime = 229.18 * (0.000075
                + 0.001868 * Math.cos(gamma)
                - 0.032077 * Math.sin(gamma)
                - 0.014615 * Math.cos(2 * gamma)
                - 0.040849 * Math.sin(2 * gamma));

        double declination = 0.006918
                - 0.399912 * Math.cos(gamma)
                + 0.070257 * Math.sin(gamma)
                - 0.006758 * Math.cos(2 * gamma)
                + 0.000907 * Math.sin(2 * gamma)
                - 0.002697 * Math.cos(3 * gamma)
                + 0.00148 * Math.sin(3 * gamma);

        double latRad = Math.toRadians(latitude);
        double cosHourAngle = Math.cos(Math.toRadians(SUNRISE_ZENITH_DEGREES))
                / (Math.cos(latRad) * Math.cos(declination))
                - Math.tan(latRad) * Math.tan(declination);
        cosHourAngle = Math.max(-1.0, Math.min(1.0, cosHourAngle));
        double hourAngle = Math.toDegrees(Math.acos(cosHourAngle));

        int offset = clock.getUtcOffsetMinutes();
        int sunrise = (int) Math.round(720 - 4 * (longitude + hourAngle) - equationOfTime + offset);
        int sunset = (int) Math.round(720 - 4 * (longitude - hourAngle) - equationOfTime + offset);

        return new int[]{sunrise, sunset};
    }

    private static float calculateProgress(int currentMinutes, int sunriseMinutes, int sunsetMinutes) {
        if (currentMinutes <= sunriseMinutes) return 0f;
        if (currentMinutes >= sunsetMinutes) return 1f;
        return (float) (currentMinutes - sunriseMinutes) / (sunsetMinutes - sunriseMinutes);
    }

    private static float calculateArcPosition(int currentMinutes, int sunriseMinutes, int sunsetMinutes) {
        if (currentMinutes <= sunriseMinutes) return 0f;
        if (currentMinutes < sunsetMinutes) {
            return SUNSET_POSITION * ((float) (currentMinutes - sunriseMinutes) / (sunsetMinutes - sunriseMinutes));
        }
        if (END_OF_DAY_MINUTES <= sunsetMinutes) return SUNSET_POSITION;
        if (currentMinutes >= END_OF_DAY_MINUTES) return 1f;
        return SUNSET_POSITION + (1f - SUNSET_POSITION)
                * ((float) (currentMinutes - sunsetMinutes) / (END_OF_DAY_MINUTES - sunsetMinutes));
    }

    private static String formatTime(int totalMinutes, boolean is24Hour) {
        int minutes = Math.floorMod(totalMinutes, 1440);
        int hour24 = minutes / 60;
        int minute = minutes % 60;

        if (is24Hour) {
            return String.format(Locale.US, "%02d:%02d", hour24, minute);
        }

        int hour12 = (hour24 % 12 == 0) ? 12 : hour24 % 12;
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, hour24 >= 12 ? "PM" : "AM");
    }

    private static float bezier(float p0, float p1, float p2, float t) {
        float u = 1f - t;
        return u * u * p0 + 2f * u * t * p1 + t * t * p2;
    }

    private static void drawArc(Canvas canvas, Paint paint, float position) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setColor(0x55FFFFFF);

        Path fullArc = new Path();
        fullArc.moveTo(START_X, START_Y);
        fullArc.quadTo(CONTROL_X, CONTROL_Y, END_X, END_Y);
        canvas.drawPath(fullArc, paint);

        if (position > 0f) {
            float splitX = START_X + (CONTROL_X - START_X) * position;
            float splitY = START_Y + (CONTROL_Y - START_Y) * position;

            Path traveledArc = new Path();
            traveledArc.moveTo(START_X, START_Y);
            traveledArc.quadTo(
                    splitX, splitY,
                    bezier(START_X, CONTROL_X, END_X, position),
                    bezier(START_Y, CONTROL_Y, END_Y, position));

            paint.setStrokeWidth(8f);
            paint.setColor(0xFFFFCC00);
            canvas.drawPath(traveledArc, paint);
        }
    }

    private static void drawSun(Canvas canvas, Paint paint, float position, boolean isDay) {
        float x = bezier(START_X, CONTROL_X, END_X, position);
        float y = bezier(START_Y, CONTROL_Y, END_Y, position);

        paint.setStyle(Paint.Style.FILL);
        if (isDay) {
            paint.setColor(0x44FFD700);
            canvas.drawCircle(x, y, 28f, paint);
            paint.setColor(0xFFFFD700);
            canvas.drawCircle(x, y, 16f, paint);
        } else {
            paint.setColor(0xFFE0E0E0);
            canvas.drawCircle(x, y, 16f, paint);
        }
    }

    private static void drawLabels(Canvas canvas, Paint paint, String sunriseText, String sunsetText) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30f);
        paint.setFakeBoldText(true);

        paint.setColor(0xDDFFFFFF);
        canvas.drawText("Sunrise", 60, 205, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText(sunriseText, 60, 242, paint);

        paint.setColor(0xDDFFFFFF);
        canvas.drawText("Sunset", 600, 205, paint);
        paint.setColor(0xFFFFFFFF);
        canvas.drawText(sunsetText, 600, 242, paint);
    }

    public static float getSunProgress(String timeZoneId) {
        ClockData clock = new ClockData(timeZoneId);
        int[] sunTimes = calculateSunriseSunsetMinutes(clock);
        return calculateProgress(clock.getMinutesOfDay(), sunTimes[0], sunTimes[1]);
    }

    public static Bitmap drawSolarArc(Context context, String timeZoneId) {
        ClockData clock = new ClockData(timeZoneId);
        int[] sunTimes = calculateSunriseSunsetMinutes(clock);
        int now = clock.getMinutesOfDay();
        float dayProgress = calculateProgress(now, sunTimes[0], sunTimes[1]);
        boolean isDay = dayProgress > 0f && dayProgress < 1f;
        float position = calculateArcPosition(now, sunTimes[0], sunTimes[1]);
        boolean is24Hour = DateFormat.is24HourFormat(context);

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        drawArc(canvas, paint, position);
        drawSun(canvas, paint, position, isDay);
        drawLabels(canvas, paint, formatTime(sunTimes[0], is24Hour), formatTime(sunTimes[1], is24Hour));

        return bitmap;
    }
}