package com.yukadeeca.service_erp.user.repository;

import com.yukadeeca.service_erp.user.entity.UserOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpVerificationRepository extends JpaRepository<UserOtpVerification, Long> {

    Optional<UserOtpVerification> findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeAndUsedAtIsNotNullOrderByUsedAtDesc(Long userId, String ipAddress, String userAgent, String otpType);

    Optional<UserOtpVerification> findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(Long userId, String ipAddress, String userAgent, String otpType);
}
