package com.patikadev.definex.advancedtaskmanager.model.enums;

public enum TaskState {
    BACKLOG("BACKLOG"),
    IN_ANALYSIS("IN_ANALYSIS"),
    IN_PROGRESS("IN_DEVELOPMENT/PROGRESS"),
    BLOCKED("BLOCKED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED");

    private final String state;

    TaskState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
} 