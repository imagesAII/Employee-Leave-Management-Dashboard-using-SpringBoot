package com.leave.management.service.impl;

import com.leave.management.dto.LeaveDTOs.*;
import com.leave.management.entity.LeaveApplication;
import com.leave.management.entity.LeaveBalance;
import com.leave.management.entity.User;
import com.leave.management.enums.LeaveStatus;
import com.leave.management.enums.LeaveType;
import com.leave.management.exception.LeaveException;
import com.leave.management.exception.ResourceNotFoundException;
import com.leave.management.repository.LeaveApplicationRepository;
import com.leave.management.repository.LeaveBalanceRepository;
import com.leave.management.repository.UserRepository;
import com.leave.management.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveApplicationRepository leaveRepo;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository balanceRepository;

    @Override
    @Transactional
    public LeaveResponse applyLeave(LeaveRequest request, String employeeEmail) {
        User employee = findUserByEmail(employeeEmail);

        validateLeaveDates(request.getStartDate(), request.getEndDate());
        checkForOverlappingLeaves(employee, request.getStartDate(), request.getEndDate());

        int totalDays = calculateWorkingDays(request.getStartDate(), request.getEndDate());
        validateLeaveBalance(employee, request.getLeaveType(), totalDays);

        LeaveApplication application = LeaveApplication.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return mapToResponse(leaveRepo.save(application));
    }

    @Override
    @Transactional
    public LeaveResponse reviewLeave(Long leaveId, ReviewRequest request, String reviewerEmail) {
        LeaveApplication application = findLeaveById(leaveId);
        User reviewer = findUserByEmail(reviewerEmail);

        if (application.getStatus() != LeaveStatus.PENDING) {
            throw new LeaveException("Only PENDING applications can be reviewed. Current status: " + application.getStatus());
        }

        if (request.getStatus() != LeaveStatus.APPROVED && request.getStatus() != LeaveStatus.REJECTED) {
            throw new LeaveException("Review status must be APPROVED or REJECTED");
        }

        application.setStatus(request.getStatus());
        application.setReviewedBy(reviewer);
        application.setReviewComments(request.getReviewComments());
        application.setReviewedAt(LocalDateTime.now());

        if (request.getStatus() == LeaveStatus.APPROVED) {
            deductLeaveBalance(application.getEmployee(), application.getLeaveType(), application.getTotalDays());
        }

        return mapToResponse(leaveRepo.save(application));
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId, String employeeEmail) {
        LeaveApplication application = findLeaveById(leaveId);
        User employee = findUserByEmail(employeeEmail);

        if (!application.getEmployee().getId().equals(employee.getId())) {
            throw new LeaveException("You can only cancel your own leave applications");
        }

        if (application.getStatus() == LeaveStatus.APPROVED && application.getStartDate().isBefore(LocalDate.now())) {
            throw new LeaveException("Cannot cancel an already started approved leave");
        }

        if (application.getStatus() == LeaveStatus.REJECTED || application.getStatus() == LeaveStatus.CANCELLED) {
            throw new LeaveException("Cannot cancel a " + application.getStatus() + " leave");
        }

        // Restore balance if was approved
        if (application.getStatus() == LeaveStatus.APPROVED) {
            restoreLeaveBalance(employee, application.getLeaveType(), application.getTotalDays());
        }

        application.setStatus(LeaveStatus.CANCELLED);
        return mapToResponse(leaveRepo.save(application));
    }

    @Override
    public LeaveResponse getLeaveById(Long leaveId) {
        return mapToResponse(findLeaveById(leaveId));
    }

    @Override
    public List<LeaveResponse> getMyLeaves(String employeeEmail) {
        User employee = findUserByEmail(employeeEmail);
        return leaveRepo.findByEmployeeOrderByAppliedAtDesc(employee)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getAllLeaves() {
        return leaveRepo.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getLeavesByStatus(LeaveStatus status) {
        return leaveRepo.findByStatus(status).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getLeavesByDepartment(String department, LeaveStatus status) {
        return leaveRepo.findByDepartmentAndStatus(department, status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public LeaveBalanceResponse getLeaveBalance(String employeeEmail) {
        User employee = findUserByEmail(employeeEmail);
        LeaveBalance balance = balanceRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for: " + employeeEmail));
        return mapBalanceToResponse(balance, employee);
    }

    @Override
    public LeaveBalanceResponse getLeaveBalanceByEmployeeId(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));
        LeaveBalance balance = balanceRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee: " + employeeId));
        return mapBalanceToResponse(balance, employee);
    }

    // ===== Business Validation =====

    private void validateLeaveDates(LocalDate start, LocalDate end) {
        if (start.isBefore(LocalDate.now())) {
            throw new LeaveException("Start date cannot be in the past");
        }
        if (end.isBefore(start)) {
            throw new LeaveException("End date cannot be before start date");
        }
    }

    private void checkForOverlappingLeaves(User employee, LocalDate start, LocalDate end) {
        List<LeaveApplication> overlapping = leaveRepo.findOverlappingLeaves(employee, start, end);
        if (!overlapping.isEmpty()) {
            throw new LeaveException("You already have an approved leave during this period");
        }
    }

    private void validateLeaveBalance(User employee, LeaveType leaveType, int days) {
        LeaveBalance balance = balanceRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found"));

        int available = getAvailableBalance(balance, leaveType);
        if (days > available) {
            throw new LeaveException("Insufficient " + leaveType + " leave balance. Available: " + available + ", Requested: " + days);
        }
    }

    private int getAvailableBalance(LeaveBalance balance, LeaveType type) {
        return switch (type) {
            case ANNUAL -> balance.getAnnualLeave();
            case SICK -> balance.getSickLeave();
            case CASUAL -> balance.getCasualLeave();
            case MATERNITY -> balance.getMaternityLeave();
            case PATERNITY -> balance.getPaternityLeave();
            case COMPENSATORY -> balance.getCompensatoryLeave();
            case UNPAID -> Integer.MAX_VALUE; // Unpaid leave has no limit
        };
    }

    private void deductLeaveBalance(User employee, LeaveType type, int days) {
        LeaveBalance balance = balanceRepository.findByEmployee(employee).orElseThrow();
        switch (type) {
            case ANNUAL -> balance.setAnnualLeave(balance.getAnnualLeave() - days);
            case SICK -> balance.setSickLeave(balance.getSickLeave() - days);
            case CASUAL -> balance.setCasualLeave(balance.getCasualLeave() - days);
            case MATERNITY -> balance.setMaternityLeave(balance.getMaternityLeave() - days);
            case PATERNITY -> balance.setPaternityLeave(balance.getPaternityLeave() - days);
            case COMPENSATORY -> balance.setCompensatoryLeave(balance.getCompensatoryLeave() - days);
            default -> {} // UNPAID - no deduction
        }
        balanceRepository.save(balance);
    }

    private void restoreLeaveBalance(User employee, LeaveType type, int days) {
        LeaveBalance balance = balanceRepository.findByEmployee(employee).orElseThrow();
        switch (type) {
            case ANNUAL -> balance.setAnnualLeave(balance.getAnnualLeave() + days);
            case SICK -> balance.setSickLeave(balance.getSickLeave() + days);
            case CASUAL -> balance.setCasualLeave(balance.getCasualLeave() + days);
            case MATERNITY -> balance.setMaternityLeave(balance.getMaternityLeave() + days);
            case PATERNITY -> balance.setPaternityLeave(balance.getPaternityLeave() + days);
            case COMPENSATORY -> balance.setCompensatoryLeave(balance.getCompensatoryLeave() + days);
            default -> {}
        }
        balanceRepository.save(balance);
    }

    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY &&
                current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    // ===== Helpers =====

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private LeaveApplication findLeaveById(Long id) {
        return leaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", id));
    }

    private LeaveResponse mapToResponse(LeaveApplication la) {
        return LeaveResponse.builder()
                .id(la.getId())
                .employeeId(la.getEmployee().getId())
                .employeeName(la.getEmployee().getFullName())
                .department(la.getEmployee().getDepartment())
                .leaveType(la.getLeaveType())
                .startDate(la.getStartDate())
                .endDate(la.getEndDate())
                .totalDays(la.getTotalDays())
                .reason(la.getReason())
                .status(la.getStatus())
                .reviewedByName(la.getReviewedBy() != null ? la.getReviewedBy().getFullName() : null)
                .reviewComments(la.getReviewComments())
                .appliedAt(la.getAppliedAt())
                .reviewedAt(la.getReviewedAt())
                .build();
    }

    private LeaveBalanceResponse mapBalanceToResponse(LeaveBalance lb, User employee) {
        return LeaveBalanceResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .annualLeave(lb.getAnnualLeave())
                .sickLeave(lb.getSickLeave())
                .casualLeave(lb.getCasualLeave())
                .maternityLeave(lb.getMaternityLeave())
                .paternityLeave(lb.getPaternityLeave())
                .compensatoryLeave(lb.getCompensatoryLeave())
                .year(lb.getYear())
                .build();
    }
}
