package com.patikadev.definex.advancedtaskmanager.controller;

import com.patikadev.definex.advancedtaskmanager.constant.SuccessMessages;
import com.patikadev.definex.advancedtaskmanager.model.dto.common.ApiResponse;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;
import com.patikadev.definex.advancedtaskmanager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@Valid @RequestBody CreateCommentRequest request) {
        log.info("Request received to create a new comment for task ID: {}", request.getTaskId());
        CommentResponse response = commentService.createComment(request);
        log.info("Comment created successfully with ID: {}", response.getId());
        return ResponseEntity.ok(ApiResponse.created(SuccessMessages.COMMENT_CREATED, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request) {
        log.info("Request received to update comment with ID: {}", id);
        CommentResponse response = commentService.updateComment(id, request);
        log.info("Comment updated successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.COMMENT_UPDATED, response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long id) {
        log.info("Request received to get comment details for ID: {}", id);
        CommentResponse response = commentService.getCommentById(id);
        log.info("Comment details retrieved successfully for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.COMMENTS_FETCHED, response));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByTaskId(@PathVariable UUID taskId) {
        log.info("Request received to get comments for task ID: {}", taskId);
        List<CommentResponse> responses = commentService.getCommentsByTaskId(taskId);
        log.info("Retrieved {} comments for task ID: {}", responses.size(), taskId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.COMMENTS_FETCHED, responses));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER')")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByUserId(@PathVariable UUID userId) {
        log.info("Request received to get comments by user ID: {}", userId);
        List<CommentResponse> responses = commentService.getCommentsByUserId(userId);
        log.info("Retrieved {} comments for user ID: {}", responses.size(), userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.COMMENTS_FETCHED, responses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_GROUP_MANAGER', 'PROJECT_MANAGER', 'TEAM_LEADER', 'TEAM_MEMBER')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        log.info("Request received to delete comment with ID: {}", id);
        commentService.deleteComment(id);
        log.info("Comment deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.COMMENT_DELETED));
    }
} 