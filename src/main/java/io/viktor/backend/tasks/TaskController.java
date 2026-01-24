package io.viktor.backend.tasks;

import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.tasks.dto.TaskResponse;
import jakarta.validation.Valid;
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

    @PostMapping
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest req) {
        return service.create(req);
    }
}
