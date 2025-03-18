package com.yukadeeca.service_erp.common.constant.emailTemplate;

import java.util.Map;

public interface EmailTemplateBuilder {

    String getSubject();

    String getTemplate();

    Map<String, Object> build();

}
