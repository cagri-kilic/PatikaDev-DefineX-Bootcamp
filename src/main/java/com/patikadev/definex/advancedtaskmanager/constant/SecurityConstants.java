package com.patikadev.definex.advancedtaskmanager.constant;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new IllegalStateException("Constant class");
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String JWT_AUTHORITIES_KEY = "authorities";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final String[] PUBLIC_URLS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh-token"
    };

    public static final String INVALID_JWT_SIGNATURE = "Invalid JWT signature";
    public static final String INVALID_JWT_TOKEN = "Invalid JWT token";
    public static final String EXPIRED_JWT_TOKEN = "Expired JWT token";
    public static final String UNSUPPORTED_JWT_TOKEN = "Unsupported JWT token";
    public static final String EMPTY_JWT_CLAIMS = "JWT claims string is empty";
    public static final String AUTH_CONTEXT_ERROR = "Could not set user authentication in security context";
} 