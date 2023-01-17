package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.QueueManagementSystemServerApplication;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.ClientInQueue;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
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
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            QueueService queueService,
            CleanerService cleanerService,
            BoardService boardService,
            SimpMessagingTemplate messagingTemplate,
            QueueRepo queueRepo,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.queueService = queueService;
        this.cleanerService = cleanerService;
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
        this.queueRepo = queueRepo;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientCodeRepo = clientCodeRepo;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public QueueStateForClient joinQueue(Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException {
        if (joinQueueRequest.getFirstName().isEmpty()) {
            throw new DescriptionException("Имя не может быть пустым");
        }
        if (joinQueueRequest.getFirstName().length() > 64) {
            throw new DescriptionException("Имя должно содержать меньше 64 символов");
        }
        if (joinQueueRequest.getLastName().isEmpty()) {
            throw new DescriptionException("Фамилия не может быть пустой");
        }
        if (joinQueueRequest.getLastName().length() > 64) {
            throw new DescriptionException("Фамилия должна содержать меньше 64 символов");
        }
        if (!Util.emailMatches(joinQueueRequest.getEmail())) {
            throw new DescriptionException("Некорректная почта");
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException("Очередь не существует");
        }
        List<ClientInQueueEntity> clientsEntities = clients.get();

        if (clientInQueueRepo.existsByQueueIdAndEmail(
                queueId,
                joinQueueRequest.getEmail()
        )) {
            throw new DescriptionException("Клиент с почтой " + joinQueueRequest.getEmail() + " уже стоит в очереди");
        }

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

        int publicCode;
        List<Integer> publicCodes = clientsEntities.stream().map(ClientInQueueEntity::getPublicCode).toList();
        Optional<Integer> minOptional = publicCodes.stream().min(Integer::compare);
        if (minOptional.isEmpty()) {
            publicCode = 1;
        } else {
            int min = minOptional.get();
            if (min > 1) {
                publicCode = min - 1;
            } else {
                publicCode = publicCodes.stream().max(Integer::compare).get() + 1;
            }
        }

        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                null,
                queueId,
                joinQueueRequest.getEmail(),
                joinQueueRequest.getFirstName(),
                joinQueueRequest.getLastName(),
                curOrderNumber,
                publicCode,
                code,
                ClientInQueueStatusEntity.RESERVED
        );
        clientInQueueRepo.save(clientInQueueEntity);

        clientsEntities.add(clientInQueueEntity);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

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
        mailMessage.setSubject("Подтверждение подключения к очереди");
        mailMessage.setText("Код для подтверждения подключения к очереди: " + code);
        mailSender.send(mailMessage);

        return QueueStateForClient.toModel(curQueueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
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
        if (!Util.emailMatches(email)) {
            throw new DescriptionException("Некорректная почта");
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент с почтой " + email + " не стоит в очереди");
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
        mailMessage.setSubject("Подтверждение переподключения к очереди");
        mailMessage.setText("Код для подтверждения переподключения к очереди: " + code);
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
            throw new DescriptionException(
                    "Время действия кода истекло. Пожалуйста, попробуйте подключиться заново"
            );
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент с почтой " + email + " не стоит в очереди");
        }
        ClientCodeEntity clientCodeEntity = clientCode.get();
        if (!Objects.equals(clientCodeEntity.getCode(), code)) {
            throw new DescriptionException("Неверный код");
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueEntity.setAccessKey(clientCodeEntity.getCode());
        clientInQueueEntity.setStatus(ClientInQueueStatusEntity.CONFIRMED);
        clientInQueueRepo.save(clientInQueueEntity);
        clientCodeRepo.delete(clientCodeEntity);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        return QueueStateForClient.toModel(curQueueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient leaveQueue(Long queueId, String email, String accessKey) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException("Клиент с почтой " + email + " не стоит в очереди");
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException("У вас нет прав на то, чтобы покинуть очередь. Пожалуйста, попробуйте переподключиться");
        }

        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByEmail(email);

        QueueState curQueueState = getQueueState(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        return QueueStateForClient.toModel(curQueueState);
    }

    private QueueState getQueueState(Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException("Очередь не существует");
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new IllegalStateException("Queue with id " + queueId + "exists in QueueRepo but does not exist in ClientInQueueRepo");
        }

        QueueEntity queueEntity = queue.get();
        List<ClientInQueueEntity> clientsEntities = clients.get();

        return new QueueState(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(ClientInQueue::toModel)
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                null
        );
    }
}
