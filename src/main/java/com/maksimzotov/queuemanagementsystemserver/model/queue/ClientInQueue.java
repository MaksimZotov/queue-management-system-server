package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor
@Jacksonized
public class ClientInQueue {
    Long id;
    @JsonProperty("email")
    String email;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;
    @JsonProperty("order_number")
    Integer orderNumber;
    @JsonProperty("public_code")
    Integer publicCode;
    @JsonProperty("access_key")
    String accessKey;
    String status;


    public static ClientInQueue toModel(ClientInQueueEntity entity) {
        return new ClientInQueue(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getOrderNumber(),
                entity.getPublicCode(),
                entity.getAccessKey(),
                entity.getStatus()
        );
    }
}
