package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.regex.Pattern;

public class PhoneChecker {
    private static final String PHONE_PATTERN = "^[\\+]?[0-9]{11}";

    public static boolean phoneMatches(String phone) {
        return Pattern.compile(PHONE_PATTERN)
                .matcher(phone)
                .matches();
    }
}
