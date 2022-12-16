package com.maksimzotov.queuemanagementsystemserver.model.verification;

import lombok.Value;

@Value
public class ConfirmCodeRequest {
    String username;
    String code;
}
