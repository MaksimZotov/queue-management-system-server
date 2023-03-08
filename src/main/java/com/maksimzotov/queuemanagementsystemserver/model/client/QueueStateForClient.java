package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import lombok.Value;

import java.util.Objects;

@Value
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

    public static QueueStateForClient toModel(QueueStateModel queueStateModel, ClientInQueueEntity clientInQueueEntity, ClientEntity clientEntity) {
        String accessKey = null;
        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
            accessKey = clientEntity.getAccessKey();
        }
        return new QueueStateForClient(
                true,
                queueStateModel.getName(),
                queueStateModel.getClients().size(),
                clientEntity.getEmail(),
                clientEntity.getFirstName(),
                clientEntity.getLastName(),
                clientInQueueEntity.getOrderNumber() - 1,
                clientInQueueEntity.getPublicCode(),
                accessKey,
                clientEntity.getStatus()
        );
    }
}
