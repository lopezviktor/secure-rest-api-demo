package io.viktor.backend.tasks;

import io.viktor.backend.security.CurrentUser;
import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.tasks.dto.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaskResponse> getAll(@RequestParam(required = false)Long userId){
        return (userId == null) ? service.findAll() : service.findByUserId(userId);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest req) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();
        return service.create(req, currentUserId, isAdmin);
    }
}
