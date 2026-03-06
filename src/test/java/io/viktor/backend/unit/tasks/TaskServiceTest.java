package io.viktor.backend.unit.tasks;

import io.viktor.backend.tasks.Task;
import io.viktor.backend.tasks.TaskRepository;
import io.viktor.backend.tasks.TaskService;
import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.users.UserRepository;
import io.viktor.backend.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyBoolean;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findAll_asAdminWithoutFilters_callsFindAll() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of());

        when(taskRepository.findAll(pageable)).thenReturn(page);

        // Act
        taskService.findAll(null, null, 1L, true, pageable);

        // Assert
        verify(taskRepository).findAll(pageable);
        verify(taskRepository, never()).findByCompleted(anyBoolean(), any(Pageable.class));
        verify(taskRepository, never()).findByUserId(anyLong(), any(Pageable.class));
        verify(taskRepository, never()).findByUserIdAndCompleted(anyLong(), anyBoolean(), any(Pageable.class));
    }

    @Test
    void findAll_asUser_ignoresRequestedUserId_andUsesCurrentUserId() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of());
        long currentUserId = 3L;
        long requestedUserId = 999L;

        when(taskRepository.findByUserId(currentUserId, pageable)).thenReturn(page);

        // Act
        taskService.findAll(requestedUserId, null, currentUserId, false, pageable);

        // Assert
        verify(taskRepository).findByUserId(currentUserId, pageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
        verify(taskRepository, never()).findByCompleted(anyBoolean(), any(Pageable.class));
        verify(taskRepository, never()).findByUserIdAndCompleted(anyLong(), anyBoolean(), any(Pageable.class));
    }

    @Test
    void findAll_asAdminWithUserIdAndCompleted_callsFindByUserIdAndCompleted() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of());
        long requestedUserId = 7L;

        when(taskRepository.findByUserIdAndCompleted(requestedUserId, true, pageable)).thenReturn(page);

        // Act
        taskService.findAll(requestedUserId, true, 1L, true, pageable);

        // Assert
        verify(taskRepository).findByUserIdAndCompleted(requestedUserId, true, pageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
        verify(taskRepository, never()).findByCompleted(anyBoolean(), any(Pageable.class));
        verify(taskRepository, never()).findByUserId(anyLong(), any(Pageable.class));
    }

    @Test
    void create_asUser_ignoresRequestedUserId_andUsesCurrentUserId() {

        // Arrange
        long currentUserId = 3L;
        long requestedUserId = 999L;
        User currentUser = org.mockito.Mockito.mock(User.class);
        when(currentUser.getId()).thenReturn(currentUserId);

        TaskCreateRequest request = new TaskCreateRequest("My task", requestedUserId);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(argThat(task ->
                "My task".equals(task.getTitle())
                        && !task.isCompleted()
                        && task.getUser() == currentUser
        ))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = taskService.create(request, currentUserId, false);

        // Assert
        verify(userRepository).findById(currentUserId);
        verify(userRepository, never()).findById(requestedUserId);
        verify(taskRepository).save(argThat(task ->
                "My task".equals(task.getTitle())
                        && !task.isCompleted()
                        && task.getUser() == currentUser
        ));

        assertEquals("My task", response.title());
        assertFalse(response.completed());
        assertEquals(currentUserId, response.userId());
    }

    @Test
    void create_asAdminWithMissingTargetUser_throwsException() {

        // Arrange
        long currentUserId = 1L;
        long requestedUserId = 7L;
        TaskCreateRequest request = new TaskCreateRequest("Admin task", requestedUserId);

        when(userRepository.findById(requestedUserId)).thenReturn(Optional.empty());

        // Act
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskService.create(request, currentUserId, true));

        // Assert
        assertEquals("User not found: 7", ex.getMessage());
        verify(userRepository).findById(requestedUserId);
        verify(taskRepository, never()).save(any(Task.class));
    }

}
