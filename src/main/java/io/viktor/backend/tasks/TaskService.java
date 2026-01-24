package io.viktor.backend.tasks;

import io.viktor.backend.users.User;
import io.viktor.backend.users.UserRepository;
import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.tasks.dto.TaskResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByUserId(Long userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest req, Long currentUserId, boolean isAdmin) {
        Long targetUserId;

        if (isAdmin) {
            targetUserId = (req.userId() != null) ? req.userId() : currentUserId;
        } else {
            targetUserId = currentUserId; // USER: siempre él mismo
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        Task task = new Task();
        task.setTitle(req.title());
        task.setCompleted(false);
        task.setUser(user);

        return toResponse(taskRepository.save(task));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.isCompleted(),
                task.getUser().getId(),
                task.getCreatedAt()
        );
    }

}
