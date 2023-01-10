package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;

public interface QueueService {
    Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException;
    void deleteQueue(String username, Long queueId) throws DescriptionException;
    ContainerForList<Queue> getQueues(Long locationId, Boolean hasRules) throws DescriptionException;
    QueueState getQueueState(Long queueId) throws DescriptionException;
    void serveClientInQueue(String username, Long queueId, Long clientId) throws DescriptionException;
    void notifyClientInQueue(String username, Long queueId, Long clientId) throws DescriptionException;
    ClientInQueue addClient(Long queueId, AddClientRequest joinQueueRequest) throws DescriptionException;
}
