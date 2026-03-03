package com.leave.management.service;

import com.leave.management.dto.LeaveDTOs.*;
import com.leave.management.enums.LeaveStatus;

import java.util.List;

public interface LeaveService {
    LeaveResponse applyLeave(LeaveRequest request, String employeeEmail);
    LeaveResponse reviewLeave(Long leaveId, ReviewRequest request, String reviewerEmail);
    LeaveResponse cancelLeave(Long leaveId, String employeeEmail);
    LeaveResponse getLeaveById(Long leaveId);
    List<LeaveResponse> getMyLeaves(String employeeEmail);
    List<LeaveResponse> getAllLeaves();
    List<LeaveResponse> getLeavesByStatus(LeaveStatus status);
    List<LeaveResponse> getLeavesByDepartment(String department, LeaveStatus status);
    LeaveBalanceResponse getLeaveBalance(String employeeEmail);
    LeaveBalanceResponse getLeaveBalanceByEmployeeId(Long employeeId);
}
