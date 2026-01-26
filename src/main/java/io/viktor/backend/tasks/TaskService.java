package io.viktor.backend.tasks;

import io.viktor.backend.tasks.dto.TaskUpdateRequest;
import io.viktor.backend.users.User;
import io.viktor.backend.users.UserRepository;
import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.tasks.dto.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(
            Long requestedUserId,
            Boolean completed,
            Long currentUserId,
            boolean isAdmin,
            Pageable pageable
    ) {
        Page<Task> page;

        if (isAdmin) {
            if (requestedUserId == null) {
                page = (completed == null)
                        ? taskRepository.findAll(pageable)
                        : taskRepository.findByCompleted(completed, pageable);
            } else {
                page = (completed == null)
                        ? taskRepository.findByUserId(requestedUserId, pageable)
                        : taskRepository.findByUserIdAndCompleted(requestedUserId, completed, pageable);
            }
        } else {
            // USER: ignore requestedUserId
            page = (completed == null)
                    ? taskRepository.findByUserId(currentUserId, pageable)
                    : taskRepository.findByUserIdAndCompleted(currentUserId, completed, pageable);
        }

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<TaskResponse> findById(Long taskId, Long currentUserId, boolean isAdmin) {

        return taskRepository.findById(taskId)
                .filter(task -> isAdmin || task.getUser().getId().equals(currentUserId))
                .map(this::toResponse);
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

    @Transactional
    public boolean deleteById(Long taskId, Long currentUserId, boolean isAdmin) {

        return taskRepository.findById(taskId)
                .filter(task -> isAdmin || task.getUser().getId().equals(currentUserId))
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<TaskResponse> updateById(Long taskId, TaskUpdateRequest req, Long currentUserId, boolean isAdmin) {
        return taskRepository.findById(taskId)
                .filter(task -> isAdmin || task.getUser().getId().equals(currentUserId))
                .map(task -> {
                    if (req.title() != null) {
                        if (req.title().isBlank()) {
                            throw new IllegalArgumentException("title must not be blank");
                        } else {
                            task.setTitle(req.title());
                        }
                    }
                    if (req.completed() != null) {
                        task.setCompleted(req.completed());
                    }
                    Task saved = taskRepository.save(task);
                    return toResponse(saved);
                });
    }

}
