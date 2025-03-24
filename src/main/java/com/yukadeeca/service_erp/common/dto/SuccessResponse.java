package com.yukadeeca.service_erp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yukadeeca.service_erp.common.constant.SuccessCode;
import lombok.Data;

import java.util.Map;

import static com.yukadeeca.service_erp.common.util.DateUtil.nowAsString;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {
    private Integer status;
    private String code;
    private String message;
    private T data;
    private Map<Object, Object> additionalProperties;
    private String timestamp;

    public SuccessResponse(SuccessCode successCode, T data, Map<Object, Object> additionalProperties) {
        this.status = successCode.getStatus();
        this.code = successCode.getCode();
        this.message = successCode.getMessage();
        this.data = data;
        this.additionalProperties = additionalProperties;
        this.timestamp = nowAsString();
    }

    public SuccessResponse(SuccessCode successCode) {
        this(successCode, null, null);
    }
}