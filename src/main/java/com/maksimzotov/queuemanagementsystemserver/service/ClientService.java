package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequst;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ClientService {
    QueueStateForClient joinByClient(Localizer localizer, AddClientRequst addClientRequst) throws DescriptionException;
    QueueStateForClient getQueueStateForClient(String email, String accessKey);
    QueueStateForClient rejoinByClient(Localizer localizer, String email) throws DescriptionException;
    QueueStateForClient confirmCodeByClient(Localizer localizer, String email, String code) throws DescriptionException;
    QueueStateForClient leaveByClient(Localizer localizer, String email, String accessKey) throws DescriptionException;
    QueueStateForClient addClientToServicesByEmployee(Localizer localizer, String accessToken, AddClientRequst addClientRequst) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueStateForClient addClientToServicesSequenceByEmployee(Localizer localizer, String accessToken, AddClientRequst addClientRequst) throws DescriptionException, AccountIsNotAuthorizedException;
    void serveClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void notifyClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void switchClientLateStateByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId, Boolean late) throws DescriptionException, AccountIsNotAuthorizedException;
}
