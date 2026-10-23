package com.example.widgetdemo.todo;

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
        findViewById(R.id.btn_cancel).setOnClickListener(view -> finish());
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
        TodoWidgetProvider.updateAllWidgets(this);
    }
}
