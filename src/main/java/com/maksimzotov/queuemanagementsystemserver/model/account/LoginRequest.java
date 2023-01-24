package com.maksimzotov.queuemanagementsystemserver.model.account;

import lombok.Value;

@Value
public class LoginRequest {
    String username;
    String password;
}
