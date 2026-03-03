package com.leave.management.dto;

import com.leave.management.enums.LeaveStatus;
import com.leave.management.enums.LeaveType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveDTOs {

    @Data
    public static class LeaveRequest {
        @NotNull(message = "Leave type is required")
        private LeaveType leaveType;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data
    public static class ReviewRequest {
        @NotNull(message = "Status is required")
        private LeaveStatus status;   // APPROVED or REJECTED

        private String reviewComments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveResponse {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String department;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalDays;
        private String reason;
        private LeaveStatus status;
        private String reviewedByName;
        private String reviewComments;
        private LocalDateTime appliedAt;
        private LocalDateTime reviewedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveBalanceResponse {
        private Long employeeId;
        private String employeeName;
        private int annualLeave;
        private int sickLeave;
        private int casualLeave;
        private int maternityLeave;
        private int paternityLeave;
        private int compensatoryLeave;
        private int year;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String department;
        private boolean active;
    }
}
