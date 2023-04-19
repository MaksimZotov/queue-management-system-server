package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.AddClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.ChangeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.client.ServeClientRequest;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ClientService {
    void addClient(Localizer localizer, Long locationId, AddClientRequest addClientRequest) throws DescriptionException;
    void changeClient(Localizer localizer, String accessToken, Long locationId, ChangeClientRequest changeClientRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void callClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void returnClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException;
    QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException;
    QueueStateForClient leaveByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException;
    void serveClientInQueueByEmployee(Localizer localizer, String accessToken, ServeClientRequest serveClientRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void notifyClientInQueueByEmployee(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteClientInLocation(Localizer localizer, String accessToken, Long locationId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
}
