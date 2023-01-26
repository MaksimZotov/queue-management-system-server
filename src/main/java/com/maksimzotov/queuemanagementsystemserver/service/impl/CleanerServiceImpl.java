package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    @Lazy
    private final QueueService queueService;
    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo;

    @Override
    public void deleteNonConfirmedUser(String username) {
        if (registrationCodeRepo.existsByUsername(username)) {
            registrationCodeRepo.deleteById(username);
            accountRepo.deleteByUsername(username);
        }
    }

    @Override
    public void deleteNonConfirmedClient(Long clientId, String email) {
        Optional<ClientEntity> client = clientRepo.findByEmail(email);
        if (client.isEmpty()) {
            return;
        }
        ClientEntity clientEntity = client.get();
        if (!Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.RESERVED.name())) {
            return;
        }
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByClientId(clientId);
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(clientId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByClientId(clientEntity.getId());
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientInQueueToChosenServiceRepo.deleteAllByClientId(clientId);
        clientRepo.deleteById(clientId);
        queueService.updateCurrentQueueState(clientId);
    }
}
