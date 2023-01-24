package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ClientService {
    QueueStateForClient joinQueue(Localizer localizer, Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException;
    QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey);
    QueueStateForClient rejoinQueue(Localizer localizer, Long queueId, String email) throws DescriptionException;
    QueueStateForClient confirmCode(Localizer localizer, Long queueId, String email, String code) throws DescriptionException;
    QueueStateForClient leaveQueue(Localizer localizer, Long queueId, String email, String accessKey) throws DescriptionException;
    QueueStateForClient addClientToService(Localizer localizer, String accessToken, Long serviceId, JoinQueueRequest joinQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueStateForClient addClientToSequence(Localizer localizer, String accessToken, Long serviceId, JoinQueueRequest joinQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
}
