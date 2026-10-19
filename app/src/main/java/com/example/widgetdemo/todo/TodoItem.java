package com.example.widgetdemo.todo;

public class TodoItem {
    private final String id;
    private final String title;
    private final boolean completed;

    public TodoItem(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted(){
        return completed;
    }
}
