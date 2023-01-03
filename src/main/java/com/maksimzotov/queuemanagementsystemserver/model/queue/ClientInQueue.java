package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Jacksonized
public class ClientInQueue {
    private Long id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("order_number")
    private Integer orderNumber;

    public static ClientInQueue toModel(ClientInQueueEntity entity) {
        return new ClientInQueue(
                entity.getPrimaryKey().getQueueId(),
                entity.getPrimaryKey().getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getOrderNumber()
        );
    }
}
