package com.example.springbootapp.service;

import com.example.springbootapp.model.Task;
import com.example.springbootapp.repository.TaskRepository;
import com.example.springbootapp.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getAllTasksForUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Admin and Moderator can see all tasks
        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_MODERATOR")) {
            return taskRepository.findAll();
        }
        
        // Regular users can only see their own tasks
        return taskRepository.findByOwnerId(userDetails.getId());
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }
}