package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.service.JobService;
import com.maksimzotov.queuemanagementsystemserver.service.SmsService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
public class SmsServiceImpl implements SmsService {

    private static final String sendMessageUrl = "https://gate.smsaero.ru/v2/sms/send";

    private final JobService jobService;
    private final String username;
    private final String password;
    private final String sign;

    private final RestTemplate restTemplate =  new RestTemplate();

    public SmsServiceImpl(
            JobService jobService,
            @Value("${app.sms.username}") String username,
            @Value("${app.sms.password}") String password,
            @Value("${app.sms.sign}") String sign
    ) {
        this.jobService = jobService;
        this.username = username;
        this.password = password;
        this.sign = sign;
    }

    @Override
    public void send(String phone, String text) {
        jobService.runAsync(() -> {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(sendMessageUrl)
                    .queryParam("number", phone)
                    .queryParam("text", text)
                    .queryParam("sign", sign);

            String resultUrl = builder.buildAndExpand(sendMessageUrl).toUri().toString();
            HttpHeaders headers = createHeaders(username, password);

            restTemplate.exchange(
                    resultUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
        });
    }

    private HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
