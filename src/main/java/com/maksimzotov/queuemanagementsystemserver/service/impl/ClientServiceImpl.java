package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.QueueManagementSystemServerApplication;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
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
import com.maksimzotov.queuemanagementsystemserver.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    public QueueStateForClient joinQueue(Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException, FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();
        if (joinQueueRequest.getFirstName().isEmpty()) {
            fieldsErrors.put(FieldsException.FIRST_NAME, "First name must not be empty");
        }
        if (joinQueueRequest.getFirstName().length() > 64) {
            fieldsErrors.put(FieldsException.FIRST_NAME, "First name must contains less then 64 symbols");
        }
        if (joinQueueRequest.getLastName().isEmpty()) {
            fieldsErrors.put(FieldsException.LAST_NAME, "Last name must not be empty");
        }
        if (joinQueueRequest.getLastName().length() > 64) {
            fieldsErrors.put(FieldsException.LAST_NAME, "Last name must contains less then 64 symbols");
        }
        if (!Util.emailMatches(joinQueueRequest.getEmail())) {
            fieldsErrors.put(FieldsException.EMAIL, "Email is incorrect");
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findByPrimaryKeyQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException("Queue does not exist");
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
    public QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) throws DescriptionException {
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
    public QueueStateForClient rejoinQueue(Long queueId, String email) throws DescriptionException {
        if (clientCodeRepo.existsById(new ClientCodeEntity.PrimaryKey(queueId, email))) {
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
    public QueueStateForClient confirmCode(Long queueId, String email, String code) throws DescriptionException {
        Optional<ClientCodeEntity> clientCode = clientCodeRepo.findById(new ClientCodeEntity.PrimaryKey(queueId, email));
        if (clientCode.isEmpty()) {
            throw new DescriptionException("Code for email " + email + "already sent. Check your email or try later");
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Client with " + email + " does not stand in queue");
        }

        ClientCodeEntity clientCodeEntity = clientCode.get();
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueEntity.setAccessKey(clientCodeEntity.getCode());
        clientInQueueEntity.setStatus(ClientInQueueStatusEntity.IN_QUEUE);
        clientInQueueRepo.save(clientInQueueEntity);
        clientCodeRepo.delete(clientCodeEntity);

        return QueueStateForClient.toModel(getQueueState(queueId), clientInQueueEntity);
    }

    @Override
    public QueueStateForClient leaveQueue(Long queueId, String email, String accessKey) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(
                new ClientInQueueEntity.PrimaryKey(
                        queueId,
                        email
                )
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Client with " + email + " does not stand in queue");
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException("You don't have rules to leave queue. Please try to rejoin to queue");
        }

        clientInQueueRepo.deleteByPrimaryKeyEmail(email);

        return QueueStateForClient.toModel(getQueueState(queueId));
    }

    private QueueState getQueueState(Long queueId) throws DescriptionException {
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
                clientsEntities.stream().map(ClientInQueue::toModel).toList(),
                null
        );
    }
}
