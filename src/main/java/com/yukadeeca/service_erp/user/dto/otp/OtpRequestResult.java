package com.yukadeeca.service_erp.user.dto.otp;

import lombok.Data;

@Data
public class OtpRequestResult {

    Boolean needToSendNew = true;
    String otp;
    Integer remainingRequestCount;
    Integer maxRequestLimit;
    Integer retryAfterSeconds;

}
