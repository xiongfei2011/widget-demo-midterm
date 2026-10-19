package com.example.widgetdemo.todo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoStorage {
    public static final int MAX_ITEMS = 10;

    private static final String PREFS_NAME = "todo_storage";
    private static final String KEY_ITEMS = "items";

    private TodoStorage() {
    }
    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }
    public static List<TodoItem> loadItem(Context context){
        String json = preferences(context).getString(KEY_ITEMS, "[]");
        List<TodoItem> items = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);

                items.add(new TodoItem(
                        object.getString("id"),
                        object.getString("title"),
                        // Dữ liệu cũ chưa có trường completed thì mặc định chưa hoàn thành.
                        object.optBoolean("completed", false)
                ));
            }
        } catch (JSONException e){
            throw new IllegalStateException("Cannot read todo data", e);
        }

        return items;
    }
    public static boolean addItem(Context context, String title){
        String cleanTitle = title.trim();

        if (cleanTitle.isEmpty()){
            return false;
        }

        List<TodoItem> items = loadItem(context);

        if (items.size() >= MAX_ITEMS){
            return false;
        }

        items.add(new TodoItem(
                UUID.randomUUID().toString(),
                cleanTitle,
                false
        ));

        saveItem(context,items);
        return true;
    }
    public static void deleteItem(Context context, String id){
        List<TodoItem> items = loadItem(context);
        items.removeIf(item -> item.getId().equals(id));
        saveItem(context,items);
    }

    private static void saveItem(Context context, List<TodoItem> items){
        JSONArray array = new JSONArray();

        try {
            for (TodoItem item : items){
                JSONObject object = new JSONObject();
                object.put("id", item.getId());
                object.put("title", item.getTitle());
                object.put("completed", item.isCompleted());
                array.put(object);
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Cannot encode todo data", e);
        }

        preferences(context).edit().putString(KEY_ITEMS, array.toString()).apply();
    }

    public static TodoItem findItem(Context context, String id){
        for (TodoItem item : loadItem(context)){
            if (item.getId().equals(id)){
                return item;
            }
        }
        return null;
    }

    public static boolean updateItem(Context context, String id, String title, boolean completed){
        String cleanTitle = title.trim();

        if (cleanTitle.isEmpty()){
            return false;
        }

        List<TodoItem> items = loadItem(context);

        for(int i = 0; i < items.size(); i++){
            TodoItem item = items.get(i);

            if (item.getId().equals(id)){
                items.set(i, new TodoItem(id, cleanTitle, completed));
                saveItem(context, items);
                return true;
            }
        }

        return false;
    }
}
