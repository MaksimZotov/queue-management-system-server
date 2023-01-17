package com.maksimzotov.queuemanagementsystemserver.model.base;

import java.util.Map;

public class ErrorResult {
    private final String description;
    private final Map<String, String> errors;

    public ErrorResult() {
        this.description = null;
        this.errors = null;
    }

    public ErrorResult(Map<String, String> errors) {
        this.description = null;
        this.errors = errors;
    }

    public ErrorResult(String description) {
        this.description = description;
        this.errors = null;
    }

    public ErrorResult(String description, Map<String, String> errors) {
        this.description = description;
        this.errors = errors;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
