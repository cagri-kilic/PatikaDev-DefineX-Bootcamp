package com.patikadev.definex.advancedtaskmanager.model.enums;

public enum ProjectStatus {
    IN_PROGRESS("IN_PROGRESS"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    PENDING("PENDING"),
    ON_HOLD("ON_HOLD"),
    PLANNING("PLANNING"),
    REVIEW("REVIEW"),
    TESTING("TESTING"),
    ARCHIVED("ARCHIVED"),
    FAILED("FAILED");

    private final String status;

    ProjectStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
} 