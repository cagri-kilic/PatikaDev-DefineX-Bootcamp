package com.patikadev.definex.advancedtaskmanager.constant;

public final class ErrorMessages {
    private ErrorMessages() {
        throw new IllegalStateException("Constant class");
    }

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String CONSTRAINT_VIOLATION = "Constraint violation";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String ACCESS_DENIED = "Access denied: %s";
    public static final String RESOURCE_NOT_FOUND = "%s not found";
    public static final String INVALID_STATE_TRANSITION = "Invalid state transition from %s to %s";
    public static final String UNAUTHORIZED = "Unauthorized access";

    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token was expired. Please make a new signin request";
    public static final String USER_INACTIVE = "User is inactive";

    public static final String FILE_NOT_FOUND = "File not found: %s";
    public static final String EMPTY_FILE = "Failed to store empty file";
    public static final String FILE_TYPE_NOT_ALLOWED = "File type not allowed: %s";
    public static final String FILE_STORAGE_ERROR = "Could not store file %s";
    public static final String MAX_UPLOAD_SIZE_EXCEEDED = "File size exceeds the maximum allowed size";

    public static final String DUPLICATE_EMAIL = "Email already exists";
    public static final String INVALID_PASSWORD_MATCH = "New password and confirmation do not match";
    public static final String DEPARTMENT_HAS_USERS = "Department cannot be deleted as it has associated users";
    public static final String DEPARTMENT_HAS_PROJECTS = "Department cannot be deleted as it has associated projects";
    public static final String DEPARTMENT_NAME_EXISTS = "Department with name %s already exists";
    public static final String PROJECT_HAS_TASKS = "Cannot delete project with existing tasks";
    public static final String USER_ROLE_MUST_BE_SPECIFIED = "User role must be specified";

    public static final String TASK_STATE_HISTORY_NOT_FOUND = "Task state history with id: %s not found";
    public static final String USER_ALREADY_IN_PROJECT = "User with ID %s is already a member of the project";
    public static final String USER_NOT_IN_PROJECT = "User with ID %s is not a member of the project";
    public static final String DEPARTMENT_NOT_FOUND = "Department with ID %s not found";
    public static final String USER_NOT_FOUND = "User with ID %s not found";
    public static final String PROJECT_NOT_FOUND = "Project with ID %s not found";
    public static final String TASK_NOT_FOUND = "Task with ID %s not found";
    public static final String REASON_REQUIRED = "Reason is required for %s state";
    public static final String TASK_STATE_CANNOT_BE_CHANGED = "Task in %s state cannot be changed";
    public static final String ATTACHMENT_NOT_FOUND = "Attachment with ID %s not found";
    public static final String COMMENT_NOT_FOUND = "Comment with ID %s not found";

    public static final String UNAUTHORIZED_DEPARTMENT_ACCESS = "Project Manager can only manage projects in their own department";
    public static final String UNAUTHORIZED_PROJECT_CREATE = "Project Manager cannot create project for department with ID %s";
    public static final String UNAUTHORIZED_PROJECT_UPDATE = "Project Manager cannot update project with ID %s from different department";

    public static final String UNAUTHORIZED_TASK_ACCESS = "You can only access tasks from projects in your department";
    public static final String UNAUTHORIZED_TASK_CREATE = "You can only create tasks for projects in your department";
    public static final String UNAUTHORIZED_TASK_UPDATE = "You can only update tasks from projects in your department";
    public static final String UNAUTHORIZED_TASK_DELETE = "You can only delete tasks from projects in your department";
    public static final String UNAUTHORIZED_TASK_ASSIGNMENT = "You can only assign tasks from projects in your department";
    public static final String TEAM_MEMBER_RESTRICTED_ACCESS = "You can only view and update task states within your department";
} 