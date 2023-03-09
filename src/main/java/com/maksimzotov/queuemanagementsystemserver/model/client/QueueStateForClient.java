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
    @JsonProperty("queue_length")
    Integer queueLength;

    String email;
    @JsonProperty("before_me")
    Integer beforeMe;
    Integer code;
    String status;

    public static QueueStateForClient toModel(QueueStateModel queueStateModel, ClientInQueueEntity clientInQueueEntity, ClientEntity clientEntity) {
        return new QueueStateForClient(
                true,
                queueStateModel.getName(),
                queueStateModel.getClients().size(),
                clientEntity.getEmail(),
                clientInQueueEntity.getOrderNumber() - 1,
                clientInQueueEntity.getCode(),
                clientEntity.getStatus()
        );
    }
}
