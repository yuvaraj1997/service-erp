package com.yukadeeca.service_erp.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import static com.yukadeeca.service_erp.common.util.DateUtil.nowDate;

@Entity
@Table(name = "user_otp_verifications", schema = "main")
@Data
public class UserOtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private String otpType;

    @Column
    private Boolean isActive;

    @Column
    private LocalDateTime usedAt;

    @Column
    private String userAgent;

    @Column
    private String ipAddress;

    @Column
    private Integer attempts;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column
    private Integer resendCount;

    @Column
    private LocalDateTime lastResendAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = nowDate();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = nowDate();
    }

}
