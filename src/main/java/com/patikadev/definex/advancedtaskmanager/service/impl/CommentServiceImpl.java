package com.patikadev.definex.advancedtaskmanager.service.impl;

import com.patikadev.definex.advancedtaskmanager.constant.ErrorMessages;
import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.CommentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Comment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.repository.CommentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.AuthService;
import com.patikadev.definex.advancedtaskmanager.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        Task task = findTaskById(request.getTaskId());
        User currentUser = findCurrentUser();

        Comment comment = commentMapper.toEntity(request, task, currentUser);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        Comment comment = findCommentById(id);
        commentMapper.updateEntityFromDto(request, comment);
        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long id) {
        Comment comment = findCommentById(id);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(UUID taskId) {
        if (!taskRepository.existsByIdAndIsActiveTrue(taskId)) {
            throw new ResourceNotFoundException(ErrorMessages.TASK_NOT_FOUND.formatted(taskId));
        }

        List<Comment> comments = commentRepository.findAllByTaskIdWithDetails(taskId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByUserId(UUID userId) {
        if (!userRepository.existsByIdAndIsActiveTrue(userId)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND.formatted(userId));
        }

        List<Comment> comments = commentRepository.findAllByUserIdAndIsActiveTrue(userId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = findCommentById(id);
        comment.setIsActive(false);
        commentRepository.save(comment);
    }

    private Task findTaskById(UUID taskId) {
        return taskRepository.findByIdAndIsActiveTrue(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.TASK_NOT_FOUND.formatted(taskId)));
    }

    private User findCurrentUser() {
        UUID currentUserId = authService.getCurrentUserId();
        return userRepository.findByIdAndIsActiveTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.USER_NOT_FOUND.formatted(currentUserId)));
    }

    private Comment findCommentById(Long id) {
        return commentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.COMMENT_NOT_FOUND.formatted(id)));
    }
} 