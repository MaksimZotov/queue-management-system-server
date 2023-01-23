package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
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

        if (clientCodeRepo.existsById(primaryKey)) {
            clientCodeRepo.deleteById(primaryKey);

            Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                    queueId,
                    email
            );

            if (clientInQueue.isPresent()) {
                ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
                clientInQueueRepo.updateClientsOrderNumberInQueue(queueId, clientInQueueEntity.getOrderNumber());
                clientInQueueRepo.deleteByEmail(email);
                queueService.updateCurrentQueueState(queueId);
            }
        }
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
