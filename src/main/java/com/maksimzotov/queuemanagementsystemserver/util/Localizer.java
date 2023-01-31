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
        return new StringBuilder()
                .append(messageSource.getMessage(messageStart.toMessageId(), null, locale))
                .append(" ")
                .append(param)
                .toString();
    }

    public String getMessage(Message messageStart, Object param, Message messageEnd) {
        return new StringBuilder()
                .append(messageSource.getMessage(messageStart.toMessageId(), null, locale))
                .append(" ")
                .append(param)
                .append(" ")
                .append(messageSource.getMessage(messageEnd.toMessageId(), null, locale))
                .toString();
    }

    public String getMessageForClientConfirmation(String link) {
        return new StringBuilder()
                .append(messageSource.getMessage(Message.PLEASE_GO_TO_LINK_TO_CONFIRM_CONNECTION.toMessageId(), null, locale))
                .append(" ")
                .append(link)
                .toString();
    }

    public String getMessageForClientCheckStatus(String queue, String publicCode, String link) {
        return new StringBuilder()
                .append(messageSource.getMessage(Message.YOUR_QUEUE.toMessageId(), null, locale))
                .append(" ")
                .append(queue)
                .append(". ")
                .append(messageSource.getMessage(Message.PUBLIC_CODE.toMessageId(), null, locale))
                .append(" ")
                .append(publicCode)
                .append(". ")
                .append(messageSource.getMessage(Message.YOU_CAN_CHECK_YOUR_STATUS_HERE.toMessageId(), null, locale))
                .append(" ")
                .append(link)
                .toString();
    }
}
