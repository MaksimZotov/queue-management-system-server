package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import org.springframework.beans.factory.annotation.Value;
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
public class ClientServiceImpl implements ClientService {

    private final MailService mailService;
    private final DelayedJobService delayedJobService;
    private final QueueService queueService;
    private final CleanerService cleanerService;
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            MailService mailService,
            DelayedJobService delayedJobService,
            QueueService queueService,
            CleanerService cleanerService,
            BoardService boardService,
            SimpMessagingTemplate messagingTemplate,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.mailService = mailService;
        this.delayedJobService = delayedJobService;
        this.queueService = queueService;
        this.cleanerService = cleanerService;
        this.boardService = boardService;
        this.messagingTemplate = messagingTemplate;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientCodeRepo = clientCodeRepo;
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
        if (!EmailChecker.emailMatches(joinQueueRequest.getEmail())) {
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
                ClientInQueueStatusEntity.Status.RESERVED.name()
        );
        clientInQueueRepo.save(clientInQueueEntity);

        clientsEntities.add(clientInQueueEntity);

        QueueState curQueueState = queueService.getQueueStateWithoutTransaction(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        delayedJobService.schedule(() ->
                {
                    try {
                        cleanerService.deleteJoinClientCode(
                                queueId,
                                joinQueueRequest.getEmail()
                        );
                    } catch (DescriptionException ignored) {}
                },
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        mailService.send(
                joinQueueRequest.getEmail(),
                "Подтверждение подключения к очереди",
                "Код для подтверждения подключения к очереди: " + code
        );

        return QueueStateForClient.toModel(curQueueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            return QueueStateForClient.toModel(queueService.getQueueStateWithoutTransaction(queueId));
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            return QueueStateForClient.toModel(queueService.getQueueStateWithoutTransaction(queueId));
        }

        return QueueStateForClient.toModel(queueService.getQueueStateWithoutTransaction(queueId), clientInQueueEntity);
    }

    @Override
    public QueueStateForClient rejoinQueue(Long queueId, String email) throws DescriptionException {
        if (!EmailChecker.emailMatches(email)) {
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

        mailService.send(
                email,
                "Подтверждение переподключения к очереди",
                "Код для подтверждения переподключения к очереди: " + code
        );

        delayedJobService.schedule(() ->
                        cleanerService.deleteRejoinClientCode(
                                queueId,
                                email
                        ),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        return QueueStateForClient.toModel(queueService.getQueueStateWithoutTransaction(queueId));
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
        clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.CONFIRMED.name());
        clientInQueueRepo.save(clientInQueueEntity);
        clientCodeRepo.delete(clientCodeEntity);

        QueueState curQueueState = queueService.getQueueStateWithoutTransaction(queueId);
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

        QueueState curQueueState = queueService.getQueueStateWithoutTransaction(queueId);
        messagingTemplate.convertAndSend("/topic/queues/" + queueId, curQueueState);
        boardService.updateLocation(curQueueState.getLocationId());

        return QueueStateForClient.toModel(curQueueState);
    }
}
