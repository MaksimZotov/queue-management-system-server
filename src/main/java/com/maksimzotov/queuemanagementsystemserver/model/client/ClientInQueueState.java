package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class ClientInQueueState {
    @JsonProperty("in_queue")
    Boolean inQueue;

    @JsonProperty("queue_name")
    String queueName;
    @JsonProperty("queue_length")
    Integer queueLength;

    String email;
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;
    @JsonProperty("before_me")
    Integer beforeMe;

    public static ClientInQueueState toModel(QueueState queueState) {
        return new ClientInQueueState(
                false,
                queueState.getName(),
                queueState.getClients().size(),
                null,
                null,
                null,
                null
        );
    }

    public static ClientInQueueState toModel(QueueState queueState, ClientInQueueStatusEntity entity) {
        return new ClientInQueueState(
                true,
                queueState.getName(),
                queueState.getClients().size(),
                entity.getClientEmail(),
                entity.getClientFirstName(),
                entity.getClientLastName(),
                entity.getClientOrderNumber() - 1
        );
    }
}
