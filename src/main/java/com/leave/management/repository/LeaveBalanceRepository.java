package com.leave.management.repository;

import com.leave.management.entity.LeaveBalance;
import com.leave.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployee(User employee);
    Optional<LeaveBalance> findByEmployeeAndYear(User employee, int year);
}
