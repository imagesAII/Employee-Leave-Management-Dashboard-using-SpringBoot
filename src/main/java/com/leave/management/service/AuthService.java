package com.leave.management.service;

import com.leave.management.dto.AuthDTOs.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
