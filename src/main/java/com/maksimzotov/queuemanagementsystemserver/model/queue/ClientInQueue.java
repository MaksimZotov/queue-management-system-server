package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
public class ClientInQueue {
    Long id;
    @JsonProperty("email")
    String email;
    Integer code;
    String status;
    List<String> services;


    public static ClientInQueue toModel(
            ClientEntity clientEntity,
            List<String> services
    ) {
        return new ClientInQueue(
                clientEntity.getId(),
                clientEntity.getEmail(),
                clientEntity.getCode(),
                clientEntity.getStatus(),
                services
        );
    }
}
