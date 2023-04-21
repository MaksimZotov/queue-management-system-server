package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.service.JobService;
import com.maksimzotov.queuemanagementsystemserver.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JobService jobService;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;

    public MailServiceImpl(
            JobService jobService,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender
    ) {
        this.jobService = jobService;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
    }

    @Override
    public void send(String to, String subject, String text) {
        jobService.runAsync(() -> {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(emailUsernameSender);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            mailSender.send(mailMessage);
        });
    }
}
