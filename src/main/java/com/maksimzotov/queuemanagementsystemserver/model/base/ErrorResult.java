package com.maksimzotov.queuemanagementsystemserver.model.base;

import java.util.Map;

public class ErrorResult {
    private final String description;
    private final Map<String, String> items;

    public ErrorResult() {
        this.description = null;
        this.items = null;
    }

    public ErrorResult(Map<String, String> items) {
        this.description = null;
        this.items = items;
    }

    public ErrorResult(String description) {
        this.description = description;
        this.items = null;
    }

    public ErrorResult(String description, Map<String, String> items) {
        this.description = description;
        this.items = items;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getItems() {
        return items;
    }
}
