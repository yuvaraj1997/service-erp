package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.UserConstants;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.UserSession;
import com.yukadeeca.service_erp.user.repository.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.yukadeeca.service_erp.common.util.DateUtil.nowDate;

@Slf4j
@Service
public class UserSessionService {

    @Autowired
    UserSessionRepository userSessionRepository;

    public void save(UserSession userSession) {
        userSessionRepository.save(userSession);
    }

    public void saveUserSession(User user, String sessionId, LocalDateTime issuedAt, LocalDateTime expiration) {
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setSessionId(sessionId);
        userSession.setIsActive(true);
        userSession.setExpiryDate(expiration);
        userSession.setCreatedAt(issuedAt);
        userSession.setUpdatedAt(issuedAt);
        save(userSession);
    }

    public UserSession findUserSessionByUserIdAndSessionId(Long userId, String sessionId) {
        return userSessionRepository.findUserSessionByUserIdAndSessionId(userId, sessionId);
    }

    public boolean isSessionActive(Long userId, String sessionId) {
        UserSession userSession = findUserSessionByUserIdAndSessionId(userId, sessionId);

        if (null == userSession || !userSession.getIsActive()) {
            return false;
        }

        boolean isExpired = nowDate().isAfter(userSession.getExpiryDate());
        boolean isUserInactive = !userSession.getUser().getStatus().equals(UserConstants.STATUS_ACTIVE);

        if (isExpired || isUserInactive) {
            userSession.setIsActive(false);
            save(userSession);
            return false;
        }

        return userSession.getIsActive();
    }

    public void markSessionAsInactive(Long userId, String sessionId) {
        UserSession userSession = findUserSessionByUserIdAndSessionId(userId, sessionId);

        if (null == userSession) {
            return;
        }

        userSession.setIsActive(false);
        save(userSession);
    }
}
