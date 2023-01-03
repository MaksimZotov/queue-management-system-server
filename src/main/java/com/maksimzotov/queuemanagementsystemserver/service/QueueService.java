package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.Queue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;

public interface QueueService {
    Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest);
    Long deleteQueue(String username, Long queueId);
    ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize, Boolean hasRules);
    QueueState getQueueState(Long queueId);
    void serveClientInQueue(String username, Long queueId, String email);
    void notifyClientInQueue(String username, Long queueId, String email);
}
