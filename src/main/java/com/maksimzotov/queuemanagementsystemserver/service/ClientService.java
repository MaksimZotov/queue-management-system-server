package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;

public interface ClientService {
    QueueStateForClient joinQueue(Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException;
    QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) throws DescriptionException;
    QueueStateForClient rejoinQueue(Long queueId, String email) throws DescriptionException;
    QueueStateForClient confirmCode(Long queueId, String email, String code) throws DescriptionException;
    QueueStateForClient leaveQueue(Long queueId, String email, String accessKey) throws DescriptionException;
}
