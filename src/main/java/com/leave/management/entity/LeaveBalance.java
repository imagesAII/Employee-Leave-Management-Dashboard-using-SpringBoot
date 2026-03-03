package com.leave.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private User employee;

    @Builder.Default
    private int annualLeave = 18;

    @Builder.Default
    private int sickLeave = 12;

    @Builder.Default
    private int casualLeave = 6;

    @Builder.Default
    private int maternityLeave = 180;

    @Builder.Default
    private int paternityLeave = 15;

    @Builder.Default
    private int compensatoryLeave = 0;

    @Builder.Default
    private int year = java.time.Year.now().getValue();
}
