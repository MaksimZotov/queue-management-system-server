package com.maksimzotov.queuemanagementsystemserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@AllArgsConstructor
@Value
@EqualsAndHashCode(callSuper = false)
public class DescriptionException extends Exception {
    String description;
}