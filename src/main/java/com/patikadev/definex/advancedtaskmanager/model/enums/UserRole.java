package com.patikadev.definex.advancedtaskmanager.model.enums;

public enum UserRole {
    ADMIN("ADMIN"),
    PROJECT_GROUP_MANAGER("PROJECT_GROUP_MANAGER"),
    PROJECT_MANAGER("PROJECT_MANAGER"),
    TEAM_LEADER("TEAM_LEADER"),
    TEAM_MEMBER("TEAM_MEMBER");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
} 