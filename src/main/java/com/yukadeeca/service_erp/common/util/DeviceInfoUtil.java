package com.yukadeeca.service_erp.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class DeviceInfoUtil {
    private DeviceInfoUtil() {
        // Prevent instantiation
    }

    public static String getClientIp(HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getUserDevice(HttpServletRequest request) {
        try {
            return request.getHeader("User-Agent");
        } catch (Exception ex) {
            return null;
        }
    }

}
