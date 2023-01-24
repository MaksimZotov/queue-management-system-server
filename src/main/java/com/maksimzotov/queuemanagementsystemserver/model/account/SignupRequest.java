package com.maksimzotov.queuemanagementsystemserver.model.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class SignupRequest {
    String username;
    String email;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;
    String password;
    @JsonProperty("repeat_password")
    String repeatPassword;
}
