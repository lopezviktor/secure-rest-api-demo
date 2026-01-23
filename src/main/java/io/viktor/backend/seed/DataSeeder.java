package io.viktor.backend.seed;

import io.viktor.backend.tasks.Task;
import io.viktor.backend.tasks.TaskRepository;
import io.viktor.backend.users.User;
import io.viktor.backend.users.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public DataSeeder(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args){
        if (userRepository.count() > 0) return;

        User admin = userRepository.save(new User("admin@demo.com", "admin123", User.Role.ADMIN));
        User user  = userRepository.save(new User("user@demo.com",  "user123",  User.Role.USER));

        taskRepository.save(new Task("Review API endpoints", false, admin));
        taskRepository.save(new Task("Add integration tests", false, admin));

        taskRepository.save(new Task("Check Swagger UI", true, user));
        taskRepository.save(new Task("Create first task", false, user));

    }
}
