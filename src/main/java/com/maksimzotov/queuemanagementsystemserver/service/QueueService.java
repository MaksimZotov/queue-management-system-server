package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;

public interface QueueService {
    Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest);
    Long deleteQueue(String username, Long id);
    ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize);
    QueueState getQueueState(Long id);
    QueueState joinQueue(Long id, JoinQueueRequest joinQueueRequest);

    Long serveClientInQueue(String username, Long id, Long clientId);
    Long notifyClientInQueue(String username, Long id, Long clientId);
}
