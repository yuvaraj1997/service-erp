package com.yukadeeca.service_erp.common.service.email.impl;

import com.yukadeeca.service_erp.common.service.email.IEmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Slf4j
@Service
@Profile("local")
public class PaperCutEmailService implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Configuration freeMakerConfiguration;

    @Value("${system.mail.from}")
    private String mailFrom;

    @Override
    public void sendHtmlEmail(String to, String subject, Map<String, Object> payload, String templatePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Set email details
            helper.setFrom(new InternetAddress(mailFrom));
            helper.setTo(to);
            helper.setSubject(subject);

            // Process Freemarker template
            Template template = freeMakerConfiguration.getTemplate(templatePath);
            StringWriter stringWriter = new StringWriter();
            template.process(payload, stringWriter);

            helper.setText(stringWriter.toString(), true); // true = enable HTML content

            // Send email
            mailSender.send(message);

            log.info("Email sent via PaperCut (Local) to={} , subject={}", to, subject);
        } catch (Exception ex) {
            log.error("{}: Unable to send html email via PaperCut (Local) to={} , subject={} , payload={} , templatePath={} , errorMessage={}",
                    ex.getClass().getSimpleName(), to, subject, payload, templatePath, ex.getMessage());
            throw new RuntimeException("Unable to send html email via PaperCut (Local)");
        }

    }

//    @Override
//    public void sendEmail(String to, String subject, Map<String, Object> model, String template) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        mailSender.send(message);
//
//        log.info("Email sent via PaperCut (Local) to={} , subject={}", to, subject);
//    }
}
