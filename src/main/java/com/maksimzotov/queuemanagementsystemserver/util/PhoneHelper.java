package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.regex.Pattern;

public class PhoneHelper {
    private static final String PHONE_PATTERN = "((\\+?7)|8)[0-9]{10}";

    public static boolean phoneMatches(String phone) {
        return Pattern.compile(PHONE_PATTERN)
                .matcher(phone)
                .matches();
    }

    public static String normalizePhoneForDatabase(String phone) {
        if (phone == null) {
            return null;
        }
        if (phone.length() == 12) {
            return "+7" + phone.substring(2);
        }
        return "+7" + phone.substring(1);
    }
}
