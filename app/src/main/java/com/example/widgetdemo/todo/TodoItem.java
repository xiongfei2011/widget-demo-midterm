package com.example.widgetdemo.todo;

public class TodoItem {
    private final String id;
    private final String title;

    public TodoItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
