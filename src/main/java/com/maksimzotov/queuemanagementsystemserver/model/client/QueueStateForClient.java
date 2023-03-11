package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import lombok.Value;

@Value
public class QueueStateForClient {
    @JsonProperty("client_id")
    Long clientId;
    @JsonProperty("location_id")
    Long locationId;
    String email;
    Integer code;
    String status;

    public static QueueStateForClient toModel(ClientEntity clientEntity) {
        return new QueueStateForClient(
                clientEntity.getId(),
                clientEntity.getLocationId(),
                clientEntity.getEmail(),
                clientEntity.getCode(),
                clientEntity.getStatus()
        );
    }
}
