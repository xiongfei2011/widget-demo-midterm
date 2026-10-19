package com.example.widgetdemo.todo;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.widgetdemo.R;
public class TodoAddActivity extends AppCompatActivity {

    private EditText input;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.todo_add_activity);

        setupInsets();

        input = findViewById(R.id.todo_input);

        findViewById(R.id.btn_save).setOnClickListener(view -> saveTodo());
    }

    private void setupInsets()
    {
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.todo_add_root),
                (view, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.ime()
                    );

                    view.setPadding(
                            padding + insets.left,
                            padding + insets.top,
                            padding + insets.right,
                            padding + insets.bottom
                    );

                    return windowInsets;
                }
        );
    }

    private void saveTodo()
    {
        String title = input.getText().toString().trim();

        if (title.isEmpty()){
            input.setError(getString(R.string.todo_error_empty));
            input.requestFocus();
            return;
        }

        try {
            boolean added = TodoStorage.addItem(this, title);

            if (!added){
                input.setError(getString(R.string.todo_error_limit, TodoStorage.MAX_ITEMS));
                return;
            }
        } catch (IllegalStateException e) {
            Toast.makeText(
                    this,
                    R.string.todo_error_storage,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        notifyWidgets();
        finish();
    }

    private void notifyWidgets(){
        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        ComponentName provider = new ComponentName(this, TodoWidgetProvider.class);

        int[] widgetIds = manager.getAppWidgetIds(provider);

        if (widgetIds.length == 0){
            return;
        }

        Intent updateIntent = new Intent(this, TodoWidgetProvider.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);

        sendBroadcast(updateIntent);
    }
}
