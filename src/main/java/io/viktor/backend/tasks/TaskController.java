package io.viktor.backend.tasks;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.viktor.backend.security.CurrentUser;
import io.viktor.backend.tasks.dto.TaskCreateRequest;
import io.viktor.backend.tasks.dto.TaskResponse;
import io.viktor.backend.tasks.dto.TaskUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.net.URI;
import io.viktor.backend.common.web.ApiPaths;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(ApiPaths.API_V1 + "/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();

        Page<TaskResponse> results = service.findAll(userId, completed, currentUserId, isAdmin, pageable);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();

        return service.findById(id, currentUserId, isAdmin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest req) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();

        TaskResponse created = service.create(req, currentUserId, isAdmin);

        URI location = URI.create("/api/tasks/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();

        boolean deleted = service.deleteById(id, currentUserId, isAdmin);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> patch(@PathVariable Long id, @RequestBody TaskUpdateRequest req) {
        Long currentUserId = CurrentUser.id();
        boolean isAdmin = CurrentUser.isAdmin();

        return service.updateById(id, req, currentUserId, isAdmin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
