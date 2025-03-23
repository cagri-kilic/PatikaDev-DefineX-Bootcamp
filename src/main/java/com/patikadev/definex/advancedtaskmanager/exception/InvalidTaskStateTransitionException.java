package com.patikadev.definex.advancedtaskmanager.exception;

public class InvalidTaskStateTransitionException extends RuntimeException {
    public InvalidTaskStateTransitionException(String message) {
        super(message);
    }
} 