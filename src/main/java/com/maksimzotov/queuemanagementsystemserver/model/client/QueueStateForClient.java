package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import lombok.Value;

@Value
public class QueueStateForClient {
    @JsonProperty("in_queue")
    Boolean inQueue;

    @JsonProperty("queue_name")
    String queueName;

    String email;
    Integer code;
    String status;

    public static QueueStateForClient toModel(QueueStateModel queueStateModel, ClientEntity clientEntity) {
        return new QueueStateForClient(
                true,
                queueStateModel.getName(),
                clientEntity.getEmail(),
                clientEntity.getCode(),
                clientEntity.getStatus()
        );
    }

    public static QueueStateForClient toModel(ClientEntity clientEntity) {
        return new QueueStateForClient(
                false,
                null,
                clientEntity.getEmail(),
                clientEntity.getCode(),
                clientEntity.getStatus()
        );
    }
}
