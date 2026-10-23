package com.example.widgetdemo.calendar;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.widgetdemo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventAddActivity extends AppCompatActivity {

    public static final String EXTRA_YEAR = "extra_year";
    public static final String EXTRA_MONTH = "extra_month";
    public static final String EXTRA_DAY = "extra_day";

    private EditText edtTitle;
    private EditText edtDateSelect;
    private EditText edtTime;
    private Button btnSave;

    private final Calendar selectedCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add_activity);

        edtTitle = findViewById(R.id.edt_event_title);
        edtDateSelect = findViewById(R.id.edt_date_select);
        edtTime = findViewById(R.id.edt_time);
        btnSave = findViewById(R.id.btn_save_event);

        int extraYear = getIntent().getIntExtra(EXTRA_YEAR, -1);
        int extraMonth = getIntent().getIntExtra(EXTRA_MONTH, -1);
        int extraDay = getIntent().getIntExtra(EXTRA_DAY, -1);

        if (extraYear != -1 && extraMonth != -1 && extraDay != -1) {
            selectedCalendar.set(extraYear, extraMonth, extraDay);
        }

        edtDateSelect.setText(dateFormat.format(selectedCalendar.getTime()));
        edtTime.setText("14:30");

        edtDateSelect.setOnClickListener(v -> {
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedCalendar.set(Calendar.YEAR, selectedYear);
                        selectedCalendar.set(Calendar.MONTH, selectedMonth);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        edtDateSelect.setText(dateFormat.format(selectedCalendar.getTime()));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString();
            String timeStr = edtTime.getText().toString().trim();

            if (title.trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề sự kiện", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = 14;
            int minute = 30;
            try {
                String[] parts = timeStr.split(":");
                if (parts.length >= 2) {
                    hour = Integer.parseInt(parts[0].trim());
                    minute = Integer.parseInt(parts[1].trim());
                } else if (parts.length == 1) {
                    hour = Integer.parseInt(parts[0].trim());
                    minute = 0;
                }
            } catch (Exception e) {
                hour = 14;
                minute = 30;
            }

            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedCalendar.set(Calendar.MINUTE, minute);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            long timestamp = selectedCalendar.getTimeInMillis();

            EventStorage.addEvent(this, title, timestamp);
            CalendarWidgetProvider.updateAllWidgets(this);

            Toast.makeText(this, "Đã thêm sự kiện thành công", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
