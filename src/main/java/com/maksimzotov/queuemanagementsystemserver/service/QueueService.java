package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.ClientInQueue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;

public interface QueueService {
    Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException;
    void deleteQueue(String username, Long queueId) throws DescriptionException;
    ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize, Boolean hasRules) throws DescriptionException;
    QueueState getQueueState(Long queueId) throws DescriptionException;
    void serveClientInQueue(String username, Long queueId, String email) throws DescriptionException;
    void notifyClientInQueue(String username, Long queueId, String email);
    ClientInQueue addClient(Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException;
}
