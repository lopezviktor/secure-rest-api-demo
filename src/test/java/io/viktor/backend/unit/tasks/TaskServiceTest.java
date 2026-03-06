package io.viktor.backend.unit.tasks;

import io.viktor.backend.tasks.Task;
import io.viktor.backend.tasks.TaskRepository;
import io.viktor.backend.tasks.TaskService;
import io.viktor.backend.users.UserRepository;
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

}
