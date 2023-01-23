package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Objects;

@Value
@Jacksonized
public class QueueStateForClient {
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
    @JsonProperty("public_code")
    Integer publicCode;
    @JsonProperty("access_key")
    String accessKey;
    String status;

    public static QueueStateForClient toModel(QueueState queueState) {
        return new QueueStateForClient(
                false,
                queueState.getName(),
                queueState.getClients().size(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static QueueStateForClient toModel(QueueState queueState, ClientInQueueEntity entity) {
        String accessKey = null;
        if (Objects.equals(entity.getStatus(), ClientInQueueStatusEntity.Status.CONFIRMED.name())) {
            accessKey = entity.getAccessKey();
        }
        return new QueueStateForClient(
                true,
                queueState.getName(),
                queueState.getClients().size(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getOrderNumber() - 1,
                entity.getPublicCode(),
                accessKey,
                entity.getStatus()
        );
    }
}
