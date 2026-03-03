package com.leave.management.controller;

import com.leave.management.dto.ApiResponse;
import com.leave.management.dto.LeaveDTOs.*;
import com.leave.management.enums.LeaveStatus;
import com.leave.management.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ManagerController {

    private final LeaveService leaveService;

    /**
     * GET /api/manager/leaves/pending
     * View all pending leave applications
     */
    @GetMapping("/leaves/pending")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getPendingLeaves() {
        List<LeaveResponse> leaves = leaveService.getLeavesByStatus(LeaveStatus.PENDING);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * GET /api/manager/leaves
     * View all leave applications (filter by status optional)
     */
    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getAllLeaves(
            @RequestParam(required = false) LeaveStatus status) {
        List<LeaveResponse> leaves = status != null
                ? leaveService.getLeavesByStatus(status)
                : leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * GET /api/manager/leaves/department/{dept}
     * View leave applications by department
     */
    @GetMapping("/leaves/department/{department}")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getLeavesByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "PENDING") LeaveStatus status) {
        List<LeaveResponse> leaves = leaveService.getLeavesByDepartment(department, status);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * PUT /api/manager/leaves/{id}/review
     * Approve or reject a leave application
     */
    @PutMapping("/leaves/{id}/review")
    public ResponseEntity<ApiResponse<LeaveResponse>> reviewLeave(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        LeaveResponse response = leaveService.reviewLeave(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave application " + request.getStatus().name().toLowerCase(), response));
    }

    /**
     * GET /api/manager/employees/{id}/balance
     * View leave balance for a specific employee
     */
    @GetMapping("/employees/{id}/balance")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getEmployeeBalance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveBalanceByEmployeeId(id)));
    }
}
