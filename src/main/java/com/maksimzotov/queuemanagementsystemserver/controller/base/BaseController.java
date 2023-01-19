package com.maksimzotov.queuemanagementsystemserver.controller.base;

import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.context.MessageSource;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public abstract class BaseController {

    public MessageSource messageSource;

    protected Localizer getLocalizer(HttpServletRequest request) {
        return new Localizer(request.getLocale(), messageSource);
    }

    protected String getToken(HttpServletRequest request) {
        return getToken(request.getHeader(AUTHORIZATION));
    }

    protected String getToken(String sourceToken) {
        if (sourceToken != null && sourceToken.startsWith("Bearer ")) {
            return sourceToken.substring("Bearer ".length());
        } else {
            return null;
        }
    }
}
