package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.exception.ResourceNotFoundException;
import com.patikadev.definex.advancedtaskmanager.mapper.CommentMapper;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.CreateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.request.comment.UpdateCommentRequest;
import com.patikadev.definex.advancedtaskmanager.model.dto.response.comment.CommentResponse;
import com.patikadev.definex.advancedtaskmanager.model.entity.Comment;
import com.patikadev.definex.advancedtaskmanager.model.entity.Project;
import com.patikadev.definex.advancedtaskmanager.model.entity.Task;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskPriority;
import com.patikadev.definex.advancedtaskmanager.model.enums.TaskState;
import com.patikadev.definex.advancedtaskmanager.repository.CommentRepository;
import com.patikadev.definex.advancedtaskmanager.repository.TaskRepository;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment;
    private User user;
    private Task task;
    private CreateCommentRequest createCommentRequest;
    private UpdateCommentRequest updateCommentRequest;
    private CommentResponse commentResponse;
    private List<Comment> commentList;
    private List<CommentResponse> commentResponseList;
    private UUID userId;
    private UUID taskId;
    private Long commentId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        commentId = 1L;

        user = createUser();
        task = createTask();
        comment = createComment();
        createCommentRequest = createCreateCommentRequest();
        updateCommentRequest = createUpdateCommentRequest();
        commentResponse = createCommentResponse();
        commentList = createCommentList();
        commentResponseList = createCommentResponseList();
    }

    @Test
    @DisplayName("Create Comment - Success")
    void createComment_Success() {
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(commentMapper.toEntity(any(CreateCommentRequest.class), any(Task.class), any(User.class))).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(commentResponse);

        CommentResponse result = commentService.createComment(createCommentRequest);

        assertNotNull(result);
        assertEquals(commentResponse, result);
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(commentMapper).toEntity(createCommentRequest, task, user);
        verify(commentRepository).save(comment);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    @DisplayName("Create Comment - Task Not Found")
    void createComment_TaskNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(createCommentRequest));

        assertTrue(exception.getMessage().contains(taskId.toString()));
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(authService, never()).getCurrentUserId();
        verify(userRepository, never()).findByIdAndIsActiveTrue(any());
        verify(commentMapper, never()).toEntity(any(), any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Comment - User Not Found")
    void createComment_UserNotFound() {
        when(taskRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.of(task));
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(userRepository.findByIdAndIsActiveTrue(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(createCommentRequest));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(authService).getCurrentUserId();
        verify(userRepository).findByIdAndIsActiveTrue(userId);
        verify(taskRepository).findByIdAndIsActiveTrue(taskId);
        verify(commentMapper, never()).toEntity(any(), any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Comment - Success")
    void updateComment_Success() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(commentResponse);

        CommentResponse result = commentService.updateComment(commentId, updateCommentRequest);

        assertNotNull(result);
        assertEquals(commentResponse, result);
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentMapper).updateEntityFromDto(updateCommentRequest, comment);
        verify(commentRepository).save(comment);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    @DisplayName("Update Comment - Not Found")
    void updateComment_NotFound() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(commentId, updateCommentRequest));

        assertTrue(exception.getMessage().contains(commentId.toString()));
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentMapper, never()).updateEntityFromDto(any(), any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Comment By Id - Success")
    void getCommentById_Success() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(comment));
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(commentResponse);

        CommentResponse result = commentService.getCommentById(commentId);

        assertNotNull(result);
        assertEquals(commentResponse, result);
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    @DisplayName("Get Comment By Id - Not Found")
    void getCommentById_NotFound() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentById(commentId));

        assertTrue(exception.getMessage().contains(commentId.toString()));
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Get Comments By Task Id - Success")
    void getCommentsByTaskId_Success() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(commentRepository.findAllByTaskIdWithDetails(any(UUID.class))).thenReturn(commentList);
        when(commentMapper.toResponseList(anyList())).thenReturn(commentResponseList);

        List<CommentResponse> result = commentService.getCommentsByTaskId(taskId);

        assertNotNull(result);
        assertEquals(commentResponseList, result);
        assertEquals(commentResponseList.size(), result.size());
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(commentRepository).findAllByTaskIdWithDetails(taskId);
        verify(commentMapper).toResponseList(commentList);
    }

    @Test
    @DisplayName("Get Comments By Task Id - Task Not Found")
    void getCommentsByTaskId_TaskNotFound() {
        when(taskRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsByTaskId(taskId));

        assertTrue(exception.getMessage().contains(taskId.toString()));
        verify(taskRepository).existsByIdAndIsActiveTrue(taskId);
        verify(commentRepository, never()).findAllByTaskIdWithDetails(any());
        verify(commentMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Get Comments By User Id - Success")
    void getCommentsByUserId_Success() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(true);
        when(commentRepository.findAllByUserIdAndIsActiveTrue(any(UUID.class))).thenReturn(commentList);
        when(commentMapper.toResponseList(anyList())).thenReturn(commentResponseList);

        List<CommentResponse> result = commentService.getCommentsByUserId(userId);

        assertNotNull(result);
        assertEquals(commentResponseList, result);
        assertEquals(commentResponseList.size(), result.size());
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(commentRepository).findAllByUserIdAndIsActiveTrue(userId);
        verify(commentMapper).toResponseList(commentList);
    }

    @Test
    @DisplayName("Get Comments By User Id - User Not Found")
    void getCommentsByUserId_UserNotFound() {
        when(userRepository.existsByIdAndIsActiveTrue(any(UUID.class))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.getCommentsByUserId(userId));

        assertTrue(exception.getMessage().contains(userId.toString()));
        verify(userRepository).existsByIdAndIsActiveTrue(userId);
        verify(commentRepository, never()).findAllByUserIdAndIsActiveTrue(any());
        verify(commentMapper, never()).toResponseList(any());
    }

    @Test
    @DisplayName("Delete Comment - Success")
    void deleteComment_Success() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.deleteComment(commentId);

        assertFalse(comment.getIsActive());
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("Delete Comment - Not Found")
    void deleteComment_NotFound() {
        when(commentRepository.findByIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(commentId));

        assertTrue(exception.getMessage().contains(commentId.toString()));
        verify(commentRepository).findByIdAndIsActiveTrue(commentId);
        verify(commentRepository, never()).save(any());
    }

    private User createUser() {
        User user = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .password("Password123!")
                .build();
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    private Task createTask() {
        Task task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("As a user, I want to test")
                .acceptanceCriteria("It should work")
                .priority(TaskPriority.HIGH)
                .state(TaskState.BACKLOG)
                .project(new Project())
                .build();
        task.setIsActive(true);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setCreatedBy("system");
        task.setUpdatedBy("system");
        return task;
    }

    private Comment createComment() {
        Comment comment = Comment.builder()
                .id(commentId)
                .content("Test Comment")
                .task(task)
                .user(user)
                .build();
        comment.setIsActive(true);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setCreatedBy("user.test@example.com");
        comment.setUpdatedBy("user.test@example.com");
        return comment;
    }

    private CreateCommentRequest createCreateCommentRequest() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Test Comment");
        request.setTaskId(taskId);
        return request;
    }

    private UpdateCommentRequest createUpdateCommentRequest() {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("Updated Test Comment");
        return request;
    }

    private CommentResponse createCommentResponse() {
        CommentResponse response = new CommentResponse();
        response.setId(commentId);
        response.setContent("Test Comment");
        response.setTaskId(taskId);
        response.setTaskTitle("Test Task");
        response.setUserId(userId);
        response.setUserName("Test User");
        response.setCreatedAt(LocalDateTime.now());
        response.setCreatedBy("user.test@example.com");
        response.setUpdatedAt(LocalDateTime.now());
        response.setUpdatedBy("user.test@example.com");
        response.setActive(true);
        return response;
    }

    private List<Comment> createCommentList() {
        return Collections.singletonList(comment);
    }

    private List<CommentResponse> createCommentResponseList() {
        return Collections.singletonList(commentResponse);
    }
} 