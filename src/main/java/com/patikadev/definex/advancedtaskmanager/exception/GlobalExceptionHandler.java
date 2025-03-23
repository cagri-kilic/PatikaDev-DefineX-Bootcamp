package com.patikadev.definex.advancedtaskmanager.exception;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.constant.HttpStatusConstants;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        log.error("Validation error: {}", errors, ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorMessages.VALIDATION_FAILED, HttpStatusConstants.BAD_REQUEST, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.add(violation.getMessage());
        });

        log.error("Constraint violation: {}", errors, ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorMessages.CONSTRAINT_VIOLATION, HttpStatusConstants.BAD_REQUEST, errors));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.NOT_FOUND));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage(), ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.BAD_REQUEST));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(String.format(ErrorMessages.ACCESS_DENIED, ex.getMessage()), HttpStatusConstants.FORBIDDEN));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage(), ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.BAD_REQUEST));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.error("Authentication failed: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorMessages.INVALID_CREDENTIALS, HttpStatusConstants.UNAUTHORIZED));
    }

    @ExceptionHandler(InvalidTaskStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTaskStateTransition(InvalidTaskStateTransitionException ex) {
        log.error("Invalid task state transition: {}", ex.getMessage(), ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.BAD_REQUEST));
    }

    @ExceptionHandler(InvalidProjectStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidProjectStatusTransition(InvalidProjectStatusTransitionException ex) {
        log.error("Invalid project status transition: {}", ex.getMessage(), ex);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.BAD_REQUEST));
    }

    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileOperation(FileOperationException ex) {
        log.error("File operation error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenRefresh(TokenRefreshException ex) {
        log.error("Token refresh error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.FORBIDDEN));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.NOT_FOUND));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorageException(FileStorageException ex) {
        log.error("File storage error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(UnauthorizedDepartmentAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedDepartmentAccess(UnauthorizedDepartmentAccessException ex) {
        log.error("Unauthorized department access: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.FORBIDDEN));
    }

    @ExceptionHandler(UnauthorizedTaskAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedTaskAccess(UnauthorizedTaskAccessException ex) {
        log.error("Unauthorized task access: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), HttpStatusConstants.FORBIDDEN));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ErrorMessages.MAX_UPLOAD_SIZE_EXCEEDED, HttpStatusConstants.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorMessages.UNEXPECTED_ERROR,
                        HttpStatusConstants.INTERNAL_SERVER_ERROR));
    }
} 