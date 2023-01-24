package com.maksimzotov.queuemanagementsystemserver.model.account;

import lombok.Value;

@Value
public class TokensResponse {
   String access;
   String refresh;
   String username;
}
