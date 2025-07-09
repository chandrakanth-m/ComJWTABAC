package com.example.springbootapp.controller;

import com.example.springbootapp.model.Task;
import com.example.springbootapp.model.auth.User;
import com.example.springbootapp.payload.response.MessageResponse;
import com.example.springbootapp.repository.UserRepository;
import com.example.springbootapp.security.abac.PolicyEvaluator;
import com.example.springbootapp.security.services.UserDetailsImpl;
import com.example.springbootapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    private final TaskService taskService;
    private final PolicyEvaluator policyEvaluator;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(TaskService taskService, PolicyEvaluator policyEvaluator, UserRepository userRepository) {
        this.taskService = taskService;
        this.policyEvaluator = policyEvaluator;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Task>> getAllTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(taskService.getAllTasksForUser(authentication));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Task> taskOpt = taskService.getTaskById(id);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOpt.get();
        if (!policyEvaluator.canAccessTask(authentication, task, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You don't have permission to view this task."));
        }
        
        return ResponseEntity.ok(task);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!policyEvaluator.canAccessTask(authentication, null, "create")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You don't have permission to create tasks."));
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User owner = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        task.setOwner(owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task taskUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Task> taskOpt = taskService.getTaskById(id);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task existingTask = taskOpt.get();
        if (!policyEvaluator.canAccessTask(authentication, existingTask, "update")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You don't have permission to update this task."));
        }
        
        taskUpdate.setId(id);
        taskUpdate.setOwner(existingTask.getOwner()); // Preserve the original owner
        return ResponseEntity.ok(taskService.updateTask(taskUpdate));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Task> taskOpt = taskService.getTaskById(id);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOpt.get();
        if (!policyEvaluator.canAccessTask(authentication, task, "delete")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You don't have permission to delete this task."));
        }
        
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}