package com.maksimzotov.queuemanagementsystemserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class DescriptionException extends Exception {
    String description;
}