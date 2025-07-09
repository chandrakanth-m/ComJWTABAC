package com.example.springbootapp.security.abac;

import com.example.springbootapp.model.Task;
import com.example.springbootapp.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class PolicyEvaluator {

    public boolean canAccessTask(Authentication authentication, Task task, String action) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Admin can do everything
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        // Moderator can read, update, and delete all tasks
        if (hasRole(authentication, "ROLE_MODERATOR")) {
            return action.equals("read") || action.equals("update") || action.equals("delete");
        }

        // User can only access their own tasks
        if (hasRole(authentication, "ROLE_USER")) {
            if (task != null && task.getOwner() != null) {
                return task.getOwner().getId().equals(userDetails.getId());
            }
            // For create operations, user can create tasks
            return action.equals("create");
        }

        return false;
    }

    public boolean canAccessUserBoard(Authentication authentication, String boardType) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        switch (boardType) {
            case "admin":
                return hasRole(authentication, "ROLE_ADMIN");
            case "moderator":
                return hasRole(authentication, "ROLE_MODERATOR") || hasRole(authentication, "ROLE_ADMIN");
            case "user":
                return hasRole(authentication, "ROLE_USER") || hasRole(authentication, "ROLE_MODERATOR") || hasRole(authentication, "ROLE_ADMIN");
            default:
                return false;
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }
}