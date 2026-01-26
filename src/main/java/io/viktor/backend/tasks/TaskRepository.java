package io.viktor.backend.tasks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository  extends JpaRepository<Task, Long> {

    Page<Task> findByUserId(Long userId, Pageable pageable);

    Page<Task> findByCompleted(Boolean completed, Pageable pageable);

    Page<Task> findByUserIdAndCompleted(Long userId, Boolean completed, Pageable pageable);
}
