package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import lombok.Value;

@Value
public class QueueStateForClient {
    @JsonProperty("client_id")
    Long clientId;
    @JsonProperty("location_id")
    Long locationId;
    String email;
    Integer code;

    public static QueueStateForClient toModel(ClientEntity clientEntity) {
        return new QueueStateForClient(
                clientEntity.getId(),
                clientEntity.getLocationId(),
                clientEntity.getEmail(),
                clientEntity.getCode()
        );
    }
}
