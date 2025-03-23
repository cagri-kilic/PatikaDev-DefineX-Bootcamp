package com.patikadev.definex.advancedtaskmanager.constant;

public final class FileConstants {
    private FileConstants() {
        throw new IllegalStateException("Constant class");
    }

    public static final int MAX_FILE_NAME_LENGTH = 255;
    public static final int MAX_CONTENT_TYPE_LENGTH = 100;

    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

    public static final String BASE_UPLOAD_DIR = "uploads";
    public static final String TASK_ATTACHMENTS_DIR = "task-attachments";
} 