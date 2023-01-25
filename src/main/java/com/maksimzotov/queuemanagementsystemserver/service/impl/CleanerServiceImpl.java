package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ClientCodeRepo clientCodeRepo;
    private final ClientRepo clientRepo;

    @Override
    public void deleteNonActivatedUser(String username) {
        if (registrationCodeRepo.existsByUsername(username)) {
            registrationCodeRepo.deleteById(username);
            accountRepo.deleteByUsername(username);
        }
    }

    @Override
    public void deleteJoinClientCode(Long queueId, String email) {
        ClientCodeEntity.PrimaryKey primaryKey = new ClientCodeEntity.PrimaryKey(
                queueId,
                email
        );
        if (!clientCodeRepo.existsById(primaryKey)) {
            return;
        }
        clientCodeRepo.deleteById(primaryKey);
        Optional<ClientEntity> client = clientRepo.findByEmail(email);
        if (client.isEmpty()) {
            return;
        }
        ClientEntity clientEntity = client.get();
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndClientId(
                queueId,
                clientEntity.getId()
        );
        if (clientInQueue.isEmpty()) {
            return;
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(queueId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByClientId(clientEntity.getId());
        queueService.updateCurrentQueueState(queueId);
    }

    @Override
    public void deleteRejoinClientCode(Long queueId, String email) {
        ClientCodeEntity.PrimaryKey primaryKey = new ClientCodeEntity.PrimaryKey(
                queueId,
                email
        );
        if (clientCodeRepo.existsById(primaryKey)) {
            clientCodeRepo.deleteById(primaryKey);
        }
    }
}
