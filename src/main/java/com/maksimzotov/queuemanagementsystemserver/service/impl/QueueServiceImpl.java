package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepo accountRepo;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;

    public QueueServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            AccountRepo accountRepo,
            LocationRepo locationRepo,
            QueueRepo queueRepo,
            ClientInQueueRepo clientInQueueRepo,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender
    ) {
        this.messagingTemplate = messagingTemplate;
        this.accountRepo = accountRepo;
        this.locationRepo = locationRepo;
        this.queueRepo = queueRepo;
        this.clientInQueueRepo = clientInQueueRepo;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
    }

    @Override
    public Queue createQueue(String username, Long locationId, CreateQueueRequest createQueueRequest) {
        QueueEntity entity = queueRepo.save(
                new QueueEntity(
                        null,
                        locationId,
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription()
                )
        );
        return Queue.toModel(entity, true);
    }

    @Override
    public void deleteQueue(String username, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Queue does not exist");
        }
        QueueEntity queueEntity = queue.get();
        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        if (location.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists without location");
        }
        LocationEntity locationEntity = location.get();
        Optional<AccountEntity> account = accountRepo.findByUsername(locationEntity.getOwnerUsername());
        if (account.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "in location with id" + locationEntity.getId() + "exists without owner");
        }
        AccountEntity accountEntity = account.get();
        if (Objects.equals(accountEntity.getUsername(), username)) {
            queueRepo.deleteById(queueId);
        } else {
            throw new DescriptionException("You are not an owner of this queue");
        }
    }

    @Override
    public ContainerForList<Queue> getQueues(Long locationId, Integer page, Integer pageSize, Boolean hasRules) throws DescriptionException {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<QueueEntity> pageResult;
        try {
            pageResult = queueRepo.findByLocationId(locationId, pageable);
        } catch (Exception ex) {
            throw new DescriptionException("Location does not exist");
        }
        return new ContainerForList<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.getContent().stream().map((item) -> Queue.toModel(item, hasRules)).toList()
        );
    }

    @Override
    public QueueState getQueueState(Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Queue does not exist");
        }
        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo but does not exist in ClientInQueueRepo");
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

    @Override
    public void serveClientInQueue(String username, Long queueId, String email) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Client with email " + email + " does not exist in queue");
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByPrimaryKeyEmail(email);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, getQueueState(queueId));
    }

    @Override
    public void notifyClientInQueue(String username, Long queueId, String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(email);
        mailMessage.setSubject("Queue");
        mailMessage.setText("Your turn!");
        mailSender.send(mailMessage);
    }
}
