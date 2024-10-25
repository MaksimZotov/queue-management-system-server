package com.maksimzotov.queuemanagementsystemserver.util;

import com.maksimzotov.queuemanagementsystemserver.message.Message;
import lombok.Value;
import org.springframework.context.MessageSource;

import java.util.Locale;

@Value
public class Localizer {
    Locale locale;
    MessageSource messageSource;

    public String getMessage(Message message) {
        return messageSource.getMessage(
                message.toMessageId(),
                null,
                locale
        );
    }

    public String getMessage(Message messageStart, Object param) {
        return messageSource.getMessage(messageStart.toMessageId(), null, locale) +
                " " +
                param;
    }

    public String getMessage(Message messageStart, Object param, Message messageEnd) {
        return messageSource.getMessage(messageStart.toMessageId(), null, locale) +
                " " +
                param +
                " " +
                messageSource.getMessage(messageEnd.toMessageId(), null, locale);
    }

    public String getMessageForClientConfirmation(String link) {
        return messageSource.getMessage(Message.PLEASE_GO_TO_LINK_TO_CONFIRM_CONNECTION.toMessageId(), null, locale) +
                " " +
                link;
    }
}
