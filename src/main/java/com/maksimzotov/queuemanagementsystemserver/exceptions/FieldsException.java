package com.maksimzotov.queuemanagementsystemserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Map;

@AllArgsConstructor
@Value
@EqualsAndHashCode(callSuper = false)
public class FieldsException extends Exception {
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String LAST_NAME = "LAST_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String REPEAT_PASSWORD = "REPEAT_PASSWORD";

    Map<String, String> errors;
}
