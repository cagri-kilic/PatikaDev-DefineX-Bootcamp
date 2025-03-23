package com.patikadev.definex.advancedtaskmanager.constant;

public final class RegexPatterns {
    private RegexPatterns() {
        throw new IllegalStateException("Constant class");
    }

    public static final String NAME_PATTERN = "^[a-zA-Z\\s]*$";
    public static final String DEPARTMENT_NAME_PATTERN = "^[a-zA-Z\\s&-]*$";
    public static final String TITLE_PATTERN = "^[a-zA-Z0-9\\s\\-_]*$";
    public static final String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).*$";

} 