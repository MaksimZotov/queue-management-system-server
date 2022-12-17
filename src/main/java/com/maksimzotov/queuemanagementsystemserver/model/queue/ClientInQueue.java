package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Jacksonized
public class ClientInQueue {
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("order_number")
    private Integer orderNumber;

    public static ClientInQueue toModel(ClientInQueueStatusEntity entity) {
        return new ClientInQueue(
                entity.getClientEmail(),
                entity.getClientFirstName(),
                entity.getClientLastName(),
                entity.getClientOrderNumber()
        );
    }
}
