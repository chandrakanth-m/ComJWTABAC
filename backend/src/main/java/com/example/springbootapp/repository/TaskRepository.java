package com.example.springbootapp.repository;

import com.example.springbootapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Spring Data JPA will automatically implement basic CRUD operations
}