package com.yukadeeca.service_erp.common.service.email;

import java.util.Map;

public interface IEmailService {

    void sendHtmlEmail(String to, String subject, Map<String, Object> model, String template);
}
