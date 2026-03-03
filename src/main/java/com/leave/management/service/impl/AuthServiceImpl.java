package com.leave.management.service.impl;

import com.leave.management.dto.AuthDTOs.*;
import com.leave.management.entity.LeaveBalance;
import com.leave.management.entity.User;
import com.leave.management.exception.LeaveException;
import com.leave.management.repository.LeaveBalanceRepository;
import com.leave.management.repository.UserRepository;
import com.leave.management.security.JwtUtils;
import com.leave.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new LeaveException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .build();

        userRepository.save(user);

        // Initialize leave balance
        LeaveBalance balance = LeaveBalance.builder()
                .employee(user)
                .build();
        leaveBalanceRepository.save(balance);

        String token = jwtUtils.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole().name(), user.getDepartment());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtUtils.generateToken(user);

        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole().name(), user.getDepartment());
    }
}
