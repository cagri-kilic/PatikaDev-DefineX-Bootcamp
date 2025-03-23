package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse createComment(CreateCommentRequest request);

    CommentResponse updateComment(Long id, UpdateCommentRequest request);

    CommentResponse getCommentById(Long id);

    List<CommentResponse> getCommentsByTaskId(UUID taskId);

    List<CommentResponse> getCommentsByUserId(UUID userId);

    void deleteComment(Long id);
} 