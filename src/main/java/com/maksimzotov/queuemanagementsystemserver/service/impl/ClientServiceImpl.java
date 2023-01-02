package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.client.ClientInQueueState;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueStatusRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final QueueRepo queueRepo;
    private final ClientInQueueStatusRepo clientInQueueStatusRepo;

    @Override
    public ClientInQueueState joinQueue(Long id, JoinQueueRequest joinQueueRequest) {
        log.info("joinQueue");
        List<Integer> orderNumbers = clientInQueueStatusRepo.findOrderNumbersInQueue(id);
        Optional<Integer> maxOrderNumber = orderNumbers.stream().max(Integer::compare);

        Integer curOrderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;

        QueueEntity queue = queueRepo.findById(id).get();
        ClientInQueueStatusEntity clientInQueueStatusEntity = new ClientInQueueStatusEntity(
                queue,
                joinQueueRequest.getEmail(),
                joinQueueRequest.getFirstName(),
                joinQueueRequest.getLastName(),
                curOrderNumber
        );
        clientInQueueStatusRepo.save(clientInQueueStatusEntity);

        QueueState curQueueState = queueService.getQueueState(id);
        messagingTemplate.convertAndSend("/topic/queues/" + id, curQueueState);

        return ClientInQueueState.toModel(curQueueState, clientInQueueStatusEntity);
    }

    @Override
    public ClientInQueueState getClientInQueueState(Long id, String email) {
        log.info("getClientInQueueState");
        ClientInQueueStatusEntity clientInQueueStatusEntity = clientInQueueStatusRepo.findByClientEmail(email);
        if (clientInQueueStatusEntity != null) {
            return ClientInQueueState.toModel(queueService.getQueueState(id), clientInQueueStatusEntity);
        } else {
            return ClientInQueueState.toModel(queueService.getQueueState(id));
        }
    }
}
