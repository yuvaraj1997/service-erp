package com.yukadeeca.service_erp.user.dto;

import lombok.Data;

@Data
public class OtpRequestResult {

    Boolean needToSendNew = true;
    String otp;

}
