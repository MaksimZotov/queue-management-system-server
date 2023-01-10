package com.maksimzotov.queuemanagementsystemserver.model.verification;

import lombok.Value;

@Value
public class TokensResponse {
   String access;
   String refresh;
   String username;
}
