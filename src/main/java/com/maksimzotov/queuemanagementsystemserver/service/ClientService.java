package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ClientService {
    ClientModel createClient(Localizer localizer, String accessToken, Long locationId, CreateClientRequest createClientRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void changeClient(Localizer localizer, String accessToken, Long locationId, Long clientId, ChangeClientRequest changeClientRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void callClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void returnClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException;
    QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException;
    void serveClient(Localizer localizer, String accessToken, Long queueId, Long clientId, ServeClientRequest serveClientRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void notifyClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteClientInLocation(Localizer localizer, String accessToken, Long locationId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException;
}
