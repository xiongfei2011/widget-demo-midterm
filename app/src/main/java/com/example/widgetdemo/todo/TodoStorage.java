package com.example.widgetdemo.todo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TodoStorage {
    public static final int MAX_ITEMS = 10;

    private static final String PREFS_NAME = "todo_storage";
    private static final String KEY_ITEMS = "items";
    private static final String KEY_SELECTED_IDS = "selected_ids";

    private TodoStorage() {
    }
    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }
    public static List<TodoItem> loadItems(Context context){
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

        List<TodoItem> items = loadItems(context);

        if (items.size() >= MAX_ITEMS){
            return false;
        }

        items.add(new TodoItem(
                UUID.randomUUID().toString(),
                cleanTitle,
                false
        ));

        saveItems(context,items);
        return true;
    }
    private static void saveItems(Context context, List<TodoItem> items){
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
        for (TodoItem item : loadItems(context)){
            if (item.getId().equals(id)){
                return item;
            }
        }
        return null;
    }

    public static void updateItem(Context context, String id, String title, boolean completed){
        String cleanTitle = title.trim();

        if (cleanTitle.isEmpty()){
            return;
        }

        List<TodoItem> items = loadItems(context);

        for(int i = 0; i < items.size(); i++){
            TodoItem item = items.get(i);

            if (item.getId().equals(id)){
                items.set(i, new TodoItem(id, cleanTitle, completed));
                saveItems(context, items);
                return;
            }
        }
    }

    public static Set<String> loadSelectedIds(Context context){
        Set<String> saveIds = preferences(context).getStringSet(KEY_SELECTED_IDS, Collections.emptySet());

        return new HashSet<>(saveIds);
    }

    public static boolean isSelected(Context context, String id){
        return loadSelectedIds(context).contains(id);
    }

    public static void toggleSelected(Context context, String id){
        Set<String> selectedIds = loadSelectedIds(context);

        if (selectedIds.contains(id)){
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }

        preferences(context)
                .edit()
                .putStringSet(KEY_SELECTED_IDS, selectedIds)
                .apply();
    }

    public static void deleteSelectedItems(Context context){
        Set<String> selectedIds = loadSelectedIds(context);

        if (selectedIds.isEmpty()){
            return;
        }

        List<TodoItem> items = loadItems(context);

        items.removeIf(item -> selectedIds.contains(item.getId()));

        saveItems(context, items);

        preferences(context)
                .edit()
                .remove(KEY_SELECTED_IDS)
                .apply();
    }
}
