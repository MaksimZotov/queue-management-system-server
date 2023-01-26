package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
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


    public static ClientInQueue toModel(ClientInQueueEntity clientInQueueEntity, ClientEntity clientEntity) {
        return new ClientInQueue(
                clientEntity.getId(),
                clientEntity.getEmail(),
                clientEntity.getFirstName(),
                clientEntity.getLastName(),
                clientInQueueEntity.getOrderNumber(),
                clientInQueueEntity.getPublicCode(),
                clientEntity.getAccessKey(),
                clientEntity.getStatus()
        );
    }
}
