package com.maksimzotov.queuemanagementsystemserver.model.verification;

import lombok.Value;

@Value
public class LoginRequest {
    String username;
    String password;
}
