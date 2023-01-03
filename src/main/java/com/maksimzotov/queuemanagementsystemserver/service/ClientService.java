package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;

public interface ClientService {
    QueueStateForClient joinQueue(Long queueId, JoinQueueRequest joinQueueRequest);
    QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey);
    QueueStateForClient rejoinQueue(Long queueId, String email);
    QueueStateForClient confirmCode(Long queueId, String email, String code);
    QueueStateForClient leaveQueue(Long queueId, String email, String accessKey);
}
