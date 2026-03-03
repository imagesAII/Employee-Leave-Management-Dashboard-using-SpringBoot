package com.leave.management.repository;

import com.leave.management.entity.LeaveApplication;
import com.leave.management.entity.User;
import com.leave.management.enums.LeaveStatus;
import com.leave.management.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    List<LeaveApplication> findByEmployee(User employee);

    List<LeaveApplication> findByEmployeeOrderByAppliedAtDesc(User employee);

    List<LeaveApplication> findByStatus(LeaveStatus status);

    List<LeaveApplication> findByEmployeeAndStatus(User employee, LeaveStatus status);

    List<LeaveApplication> findByEmployeeAndLeaveType(User employee, LeaveType leaveType);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee.department = :department AND la.status = :status")
    List<LeaveApplication> findByDepartmentAndStatus(@Param("department") String department,
                                                      @Param("status") LeaveStatus status);

    @Query("SELECT la FROM LeaveApplication la WHERE la.employee = :employee " +
           "AND la.status = 'APPROVED' " +
           "AND ((la.startDate BETWEEN :start AND :end) OR (la.endDate BETWEEN :start AND :end))")
    List<LeaveApplication> findOverlappingLeaves(@Param("employee") User employee,
                                                  @Param("start") LocalDate start,
                                                  @Param("end") LocalDate end);

    @Query("SELECT SUM(la.totalDays) FROM LeaveApplication la WHERE la.employee = :employee " +
           "AND la.leaveType = :leaveType AND la.status = 'APPROVED' " +
           "AND YEAR(la.startDate) = :year")
    Integer sumApprovedDaysByTypeAndYear(@Param("employee") User employee,
                                          @Param("leaveType") LeaveType leaveType,
                                          @Param("year") int year);
}
