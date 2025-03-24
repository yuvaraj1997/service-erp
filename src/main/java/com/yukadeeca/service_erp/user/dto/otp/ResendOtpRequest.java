package com.yukadeeca.service_erp.user.dto.otp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    String email;

}
