package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class ServeClientRequest {
    @JsonProperty("client_id")
    Long clientId;
    @JsonProperty("queue_id")
    Long queueId;
    List<Long> services;
}
