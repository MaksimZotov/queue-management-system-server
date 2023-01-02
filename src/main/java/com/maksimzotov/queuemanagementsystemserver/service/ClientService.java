package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.model.client.ClientInQueueState;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;

public interface ClientService {
    ClientInQueueState joinQueue(Long id, JoinQueueRequest joinQueueRequest);
    ClientInQueueState getClientInQueueState(Long id, String email);
}
