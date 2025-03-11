package com.yukadeeca.service_erp.user.repository;

import com.yukadeeca.service_erp.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    UserSession findUserSessionByUserIdAndSessionId(Long userId, String sessionId);

}
