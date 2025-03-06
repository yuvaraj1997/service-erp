package com.yukadeeca.service_erp.user.repository;

import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByTokenAndType(String token, String type);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.status = :newStatus WHERE vt.type = :type AND vt.status = :status AND vt.user = :user")
    void updateTokenStatus(@Param("user") User user, @Param("type") String type, @Param("status") String status, @Param("newStatus") String newStatus);
}
