package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.QueueManagementSystemServerApplication;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.ClientInQueue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final QueueService queueService;
    private final CleanerService cleanerService;
    private final SimpMessagingTemplate messagingTemplate;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            QueueService queueService,
            CleanerService cleanerService, SimpMessagingTemplate messagingTemplate,
            QueueRepo queueRepo,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.queueService = queueService;
        this.cleanerService = cleanerService;
        this.messagingTemplate = messagingTemplate;
        this.queueRepo = queueRepo;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientCodeRepo = clientCodeRepo;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public QueueStateForClient joinQueue(Long queueId, JoinQueueRequest joinQueueRequest) {
        log.info("joinQueue");

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            return null;
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            return null;
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<Integer> maxOrderNumber = clientsEntities.stream()
                .map(ClientInQueueEntity::getOrderNumber)
                .max(Integer::compare);

        Integer curOrderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;

        String code = Integer.toString(new Random().nextInt(9000) + 1000);

        clientCodeRepo.save(
                new ClientCodeEntity(
                        new ClientCodeEntity.PrimaryKey(
                                queueId,
                                joinQueueRequest.getEmail()
                        ),
                        code
                )
        );

        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        joinQueueRequest.getEmail()
                ),
                joinQueueRequest.getFirstName(),
                joinQueueRequest.getLastName(),
                curOrderNumber,
                code,
                ClientInQueueStatusEntity.RESERVED
        );
        clientInQueueRepo.save(clientInQueueEntity);

        clientsEntities.add(clientInQueueEntity);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);

        QueueManagementSystemServerApplication.scheduledExecutorService.schedule(() ->
                        cleanerService.deleteJoinClientCode(
                                queueId,
                                joinQueueRequest.getEmail()
                        ),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(joinQueueRequest.getEmail());
        mailMessage.setSubject("Join confirmation");
        mailMessage.setText("Join confirmation code: " + code);
        mailSender.send(mailMessage);

        return QueueStateForClient.toModel(curQueueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) {
        log.info("getClientInQueueState");
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            return QueueStateForClient.toModel(queueService.getQueueState(queueId));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            return QueueStateForClient.toModel(queueService.getQueueState(queueId));
        }
        return QueueStateForClient.toModel(queueService.getQueueState(queueId), clientInQueueEntity);
    }

    @Override
    public QueueStateForClient rejoinQueue(Long queueId, String email) {
        Optional<ClientCodeEntity> clientCode = clientCodeRepo.findById(new ClientCodeEntity.PrimaryKey(queueId, email));
        if (clientCode.isEmpty()) {
            return null;
        }
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            return null;
        }
        String code = Integer.toString(new Random().nextInt(9000) + 1000);
        clientCodeRepo.save(
                new ClientCodeEntity(
                        new ClientCodeEntity.PrimaryKey(
                                queueId,
                                email
                        ),
                        code
                )
        );

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(email);
        mailMessage.setSubject("Rejoin confirmation");
        mailMessage.setText("Rejoin confirmation code: " + code);
        mailSender.send(mailMessage);

        QueueManagementSystemServerApplication.scheduledExecutorService.schedule(() ->
                        cleanerService.deleteRejoinClientCode(
                                queueId,
                                email
                        ),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        return QueueStateForClient.toModel(getQueueState(queueId));
    }

    @Override
    public QueueStateForClient confirmCode(Long queueId, String email, String code) {
        Optional<ClientCodeEntity> clientCode = clientCodeRepo.findById(new ClientCodeEntity.PrimaryKey(queueId, email));
        if (clientCode.isEmpty()) {
            return null;
        }
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            return null;
        }
        ClientCodeEntity clientCodeEntity = clientCode.get();
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueEntity.setAccessKey(clientCodeEntity.getCode());
        clientInQueueEntity.setStatus(ClientInQueueStatusEntity.IN_QUEUE);
        clientInQueueRepo.save(clientInQueueEntity);
        clientCodeRepo.delete(clientCodeEntity);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            return null;
        }
        return QueueStateForClient.toModel(getQueueState(queueId), clientInQueueEntity);
    }

    @Override
    public QueueStateForClient leaveQueue(Long queueId, String email, String accessKey) {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            return null;
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            return null;

        }
        clientInQueueRepo.deleteByPrimaryKeyEmail(email);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            return null;
        }
        return QueueStateForClient.toModel(getQueueState(queueId));
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
