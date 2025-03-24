package com.yukadeeca.service_erp.user.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpRequestResponse {

    Integer remainingRequestCount;
    Integer maxRequestLimit;
    Integer retryAfterSeconds;

}
