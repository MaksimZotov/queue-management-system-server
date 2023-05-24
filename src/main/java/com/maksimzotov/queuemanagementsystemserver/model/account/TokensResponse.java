package com.maksimzotov.queuemanagementsystemserver.model.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class TokensResponse {
   String access;
   String refresh;
   @JsonProperty("account_id")
   Long accountId;
}
