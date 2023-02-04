package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ClientService {
    void addClient(Localizer localizer, Long locationId, AddClientRequst addClientRequest) throws DescriptionException;
    QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException;
    QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException;
    QueueStateForClient leaveByClient(Localizer localizer, Long clientId, String accessKey) throws DescriptionException;
    void serveClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void notifyClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
}
