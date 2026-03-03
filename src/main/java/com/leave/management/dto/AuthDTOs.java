package com.leave.management.dto;

import com.leave.management.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ===== Auth DTOs =====
public class AuthDTOs {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        @NotNull(message = "Role is required")
        private Role role;

        @NotBlank(message = "Department is required")
        private String department;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String department;

        public AuthResponse(String token, Long id, String fullName, String email, String role, String department) {
            this.token = token;
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
            this.department = department;
        }
    }
}
