package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueService {
    Queue createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<Queue> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueState getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException;
    void serveClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void notifyClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    ClientInQueue addClient(Localizer localizer, String accessToken, Long queueId, AddClientRequest joinQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueState getQueueStateWithoutTransaction(Long queueId);
    void updateQueueWithoutTransaction(Long queueId) throws DescriptionException;
}
