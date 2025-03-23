package com.patikadev.definex.advancedtaskmanager.constant;

public final class ValidationMessages {
    private ValidationMessages() {
        throw new IllegalStateException("Constant class");
    }

    public static final String FIELD_EMAIL = "Please provide a valid email address";
    public static final String FIRST_NAME_NOT_BLANK = "First name cannot be empty";
    public static final String FIRST_NAME_SIZE = "First name must be between 2 and 50 characters";
    public static final String FIRST_NAME_PATTERN = "First name can only contain letters and spaces";
    public static final String LAST_NAME_NOT_BLANK = "Last name cannot be empty";
    public static final String LAST_NAME_SIZE = "Last name must be between 2 and 50 characters";
    public static final String LAST_NAME_PATTERN = "Last name can only contain letters and spaces";
    public static final String EMAIL_NOT_BLANK = "Email cannot be empty";
    public static final String PASSWORD_NOT_BLANK = "Password cannot be empty";
    public static final String USER_ROLE_MUST_BE_SPECIFIED = "User role must be specified";
    public static final String PASSWORD_PATTERN = "Password must contain at least one digit, one lowercase, one uppercase letter and one special character";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters long";
    public static final String PASSWORD_CONFIRMATION = "Password confirmation cannot be empty";

    public static final String REFRESH_TOKEN_NOT_BLANK = "Refresh token cannot be blank";
    public static final String DEPARTMENT_NAME_NOT_BLANK = "Department name cannot be empty";
    public static final String DEPARTMENT_NAME_SIZE = "Department name must be between 2 and 100 characters";
    public static final String DEPARTMENT_NAME_PATTERN = "Department name can only contain letters, spaces, & and -";
    public static final String DEPARTMENT_DESCRIPTION_MAX_SIZE = "Description cannot exceed 500 characters";

    public static final String PROJECT_TITLE_NOT_BLANK = "Project title cannot be empty";
    public static final String PROJECT_TITLE_SIZE = "Project title must be between 2 and 100 characters";
    public static final String PROJECT_TITLE_PATTERN = "Project title can only contain letters, numbers, spaces, - and _";
    public static final String PROJECT_DESCRIPTION_MAX_SIZE = "Description cannot exceed 1000 characters";
    public static final String PROJECT_STATUS_MUST_BE_SPECIFIED = "Project status must be specified";
    public static final String PROJECT_DEPARTMENT = "Project must be assigned to a department";

    public static final String TASK_TITLE_NOT_BLANK = "Task title cannot be empty";
    public static final String TASK_TITLE_SIZE = "Task title must be between 2 and 100 characters";
    public static final String TASK_TITLE_PATTERN = "Task title can only contain letters, numbers, spaces, - and _";
    public static final String USER_STORY_NOT_BLANK = "User story cannot be empty";
    public static final String ACCEPTANCE_CRITERIA_NOT_BLANK = "Acceptance criteria cannot be empty";
    public static final String STATE_CHANGE_REASON_MAX_SIZE = "State change reason cannot exceed 500 characters";
    public static final String TASK_PROJECT = "Task must be assigned to a project";
    public static final String TASK_STATE = "Task state must be specified";
    public static final String TASK_PRIORITY = "Task priority must be specified";

    public static final String COMMENT_CONTENT_NOT_BLANK = "Comment content cannot be empty";
    public static final String COMMENT_TASK = "Comment must be associated with a task";
    public static final String COMMENT_USER = "Comment must have an author";

    public static final String FILE_NAME_NOT_BLANK = "File name cannot be empty";
    public static final String FILE_NAME_MAX_SIZE = "File name cannot exceed 255 characters";
    public static final String CONTENT_TYPE_MAX_SIZE = "Content type cannot exceed 100 characters";
    public static final String FILE_PATH = "File path cannot be empty";
    public static final String FILE_UPLOADER = "Attachment must have an uploader";
    public static final String FILE_TASK = "Attachment must be associated with a task";
} 