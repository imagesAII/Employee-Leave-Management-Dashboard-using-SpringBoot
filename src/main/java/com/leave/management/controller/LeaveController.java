package com.leave.management.controller;

import com.leave.management.dto.ApiResponse;
import com.leave.management.dto.LeaveDTOs.*;
import com.leave.management.enums.LeaveStatus;
import com.leave.management.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * POST /api/leaves/apply
     * Employee applies for leave
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody LeaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        LeaveResponse response = leaveService.applyLeave(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave application submitted successfully", response));
    }

    /**
     * GET /api/leaves/my
     * Employee views their own leave history
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getMyLeaves(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<LeaveResponse> leaves = leaveService.getMyLeaves(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * GET /api/leaves/{id}
     * Get a specific leave application by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveById(id)));
    }

    /**
     * DELETE /api/leaves/{id}/cancel
     * Employee cancels their own leave application
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        LeaveResponse response = leaveService.cancelLeave(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave application cancelled", response));
    }

    /**
     * GET /api/leaves/balance
     * Get the current user's leave balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getMyBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveBalance(userDetails.getUsername())));
    }
}
