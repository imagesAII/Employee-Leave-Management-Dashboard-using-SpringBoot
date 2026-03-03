package com.leave.management.controller;

import com.leave.management.dto.ApiResponse;
import com.leave.management.dto.LeaveDTOs.*;
import com.leave.management.entity.User;
import com.leave.management.exception.ResourceNotFoundException;
import com.leave.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    /**
     * GET /api/admin/users
     * List all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream().map(this::mapUser).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * GET /api/admin/users/{id}
     * Get a user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success(mapUser(user)));
    }

    /**
     * PATCH /api/admin/users/{id}/deactivate
     * Deactivate a user account
     */
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", mapUser(user)));
    }

    /**
     * PATCH /api/admin/users/{id}/activate
     * Reactivate a user account
     */
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User activated", mapUser(user)));
    }

    private UserResponse mapUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .department(user.getDepartment())
                .active(user.isActive())
                .build();
    }
}
