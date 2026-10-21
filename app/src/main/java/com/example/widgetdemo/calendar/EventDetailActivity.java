package com.example.widgetdemo.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.widgetdemo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_YEAR = "extra_year";
    public static final String EXTRA_MONTH = "extra_month";
    public static final String EXTRA_DAY = "extra_day";

    private TextView tvDetailDate;
    private LinearLayout layoutDetailEvents;
    private TextView tvNoDetailEvents;
    private Button btnAddOnDate;

    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_detail_activity);

        tvDetailDate = findViewById(R.id.tv_detail_date);
        layoutDetailEvents = findViewById(R.id.layout_detail_events);
        tvNoDetailEvents = findViewById(R.id.tv_no_detail_events);
        btnAddOnDate = findViewById(R.id.btn_add_on_date);

        year = getIntent().getIntExtra(EXTRA_YEAR, Calendar.getInstance().get(Calendar.YEAR));
        month = getIntent().getIntExtra(EXTRA_MONTH, Calendar.getInstance().get(Calendar.MONTH));
        day = getIntent().getIntExtra(EXTRA_DAY, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDetailDate.setText("Sự kiện ngày " + sdfDate.format(cal.getTime()));

        btnAddOnDate.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventAddActivity.class);
            intent.putExtra(EventAddActivity.EXTRA_YEAR, year);
            intent.putExtra(EventAddActivity.EXTRA_MONTH, month);
            intent.putExtra(EventAddActivity.EXTRA_DAY, day);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventsForDay();
    }

    private void loadEventsForDay() {
        layoutDetailEvents.removeAllViews();
        List<EventItem> allEvents = EventStorage.loadEvents(this);

        Calendar dayCal = Calendar.getInstance();
        dayCal.set(year, month, day, 0, 0, 0);
        long dayStart = dayCal.getTimeInMillis();
        dayCal.set(year, month, day, 23, 59, 59);
        long dayEnd = dayCal.getTimeInMillis();

        List<EventItem> dayEvents = new ArrayList<>();
        for (EventItem item : allEvents) {
            if (item.getTimestamp() >= dayStart && item.getTimestamp() <= dayEnd) {
                dayEvents.add(item);
            }
        }

        if (dayEvents.isEmpty()) {
            tvNoDetailEvents.setVisibility(View.VISIBLE);
            layoutDetailEvents.setVisibility(View.GONE);
        } else {
            tvNoDetailEvents.setVisibility(View.GONE);
            layoutDetailEvents.setVisibility(View.VISIBLE);

            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (EventItem event : dayEvents) {
                View row = getLayoutInflater().inflate(R.layout.event_detail_item, layoutDetailEvents, false);
                TextView tvTitle = row.findViewById(R.id.tv_detail_item_title);
                TextView tvTime = row.findViewById(R.id.tv_detail_item_time);
                Button btnDelete = row.findViewById(R.id.btn_delete_detail_item);

                tvTitle.setText(event.getTitle());
                tvTime.setText(sdfTime.format(new Date(event.getTimestamp())));

                btnDelete.setOnClickListener(v -> {
                    EventStorage.deleteEvent(this, event.getId());
                    CalendarWidgetProvider.updateAllWidgets(this);
                    loadEventsForDay();
                    Toast.makeText(this, "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
                });

                layoutDetailEvents.addView(row);
            }
        }
    }
}
