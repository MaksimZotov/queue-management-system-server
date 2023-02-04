package com.maksimzotov.queuemanagementsystemserver.model.account;

import lombok.Value;

@Value
public class ConfirmCodeRequest {
    String email;
    String code;
}
