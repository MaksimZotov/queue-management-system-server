package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.queue.ClientInQueue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final QueueRepo queueRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void deleteNonActivatedUser(String username) {
        log.info("Checking deletion of user with username {}", username);
        registrationCodeRepo.deleteById(username);
        accountRepo.deleteByUsername(username);
        log.info("User with username {} deleted", username);
    }

    @Override
    public void deleteJoinClientCode(Long queueId, String email) {
        log.info("Checking deletion of join code of client with email {} in queue {}", email, queueId);
        ClientCodeEntity.PrimaryKey primaryKey = new ClientCodeEntity.PrimaryKey(
                queueId,
                email
        );
        clientCodeRepo.deleteById(primaryKey);
        clientInQueueRepo.deleteByPrimaryKeyEmail(email);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);

        log.info("Join code of client with email {} in queue {} deleted", email, queueId);
    }

    @Override
    public void deleteRejoinClientCode(Long queueId, String email) {
        log.info("Checking deletion of rejoin code of client with email {} in queue {}", email, queueId);
        ClientCodeEntity.PrimaryKey primaryKey = new ClientCodeEntity.PrimaryKey(
                queueId,
                email
        );
        clientCodeRepo.deleteById(primaryKey);
        log.info("Rejoin code of client with email {} in queue {} deleted", email, queueId);
    }

    private QueueState getQueueState(Long queueId) {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            return null;
        }
        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            return null;
        }
        QueueEntity queueEntity = queue.get();
        List<ClientInQueueEntity> clientsEntities = clients.get();

        return new QueueState(
                queueId,
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream().map(ClientInQueue::toModel).toList()
        );
    }
}
