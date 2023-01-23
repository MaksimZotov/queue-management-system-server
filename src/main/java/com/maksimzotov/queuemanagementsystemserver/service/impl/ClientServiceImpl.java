package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.JoinQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.client.QueueStateForClient;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueState;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final MailService mailService;
    private final DelayedJobService delayedJobService;
    private final QueueService queueService;
    private final CleanerService cleanerService;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final Integer confirmationTimeInSeconds;

    public ClientServiceImpl(
            MailService mailService,
            DelayedJobService delayedJobService,
            QueueService queueService,
            CleanerService cleanerService,
            ClientInQueueRepo clientInQueueRepo,
            ClientCodeRepo clientCodeRepo,
            @Value("${app.registration.confirmationtime.join}")  Integer confirmationTimeInSeconds
    ) {
        this.mailService = mailService;
        this.delayedJobService = delayedJobService;
        this.queueService = queueService;
        this.cleanerService = cleanerService;
        this.clientInQueueRepo = clientInQueueRepo;
        this.clientCodeRepo = clientCodeRepo;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public QueueStateForClient joinQueue(Localizer localizer, Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException {
        List<ClientInQueueEntity> clientsEntities = checkJoinQueue(localizer, queueId, joinQueueRequest);

        Optional<Integer> maxOrderNumber = clientsEntities.stream()
                .map(ClientInQueueEntity::getOrderNumber)
                .max(Integer::compare);

        Integer orderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;
        Integer publicCode = CodeGenerator.generate(clientsEntities.stream().map(ClientInQueueEntity::getPublicCode).toList());
        String accessKey = CodeGenerator.generate();

        ClientCodeEntity clientCodeEntity = new ClientCodeEntity(
                new ClientCodeEntity.PrimaryKey(
                        queueId,
                        joinQueueRequest.getEmail()
                ),
                accessKey
        );
        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                null,
                queueId,
                joinQueueRequest.getEmail(),
                joinQueueRequest.getFirstName(),
                joinQueueRequest.getLastName(),
                orderNumber,
                publicCode,
                accessKey,
                ClientInQueueStatusEntity.Status.RESERVED.name()
        );

        clientCodeRepo.save(clientCodeEntity);
        clientInQueueRepo.save(clientInQueueEntity);

        QueueState queueState = queueService.updateCurrentQueueState(queueId);

        delayedJobService.schedule(
                () -> cleanerService.deleteJoinClientCode(queueId, joinQueueRequest.getEmail()),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        mailService.send(
                joinQueueRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_CONNECTION_TO_QUEUE),
                localizer.getMessage(Message.CODE_FOR_CONFIRMATION_OF_CONNECTION_TO_QUEUE, accessKey)
        );

        return QueueStateForClient.toModel(queueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Long queueId, String email, String accessKey) {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            return QueueStateForClient.toModel(queueService.getCurrentQueueState(queueId));
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            return QueueStateForClient.toModel(queueService.getCurrentQueueState(queueId));
        }

        return QueueStateForClient.toModel(queueService.getCurrentQueueState(queueId), clientInQueueEntity);
    }

    @Override
    public QueueStateForClient rejoinQueue(Localizer localizer, Long queueId, String email) throws DescriptionException {
        if (!EmailChecker.emailMatches(email)) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_START,
                            email,
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_END
                    )
            );
        }

        String code = CodeGenerator.generate();
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
                localizer.getMessage(Message.CONFIRMATION_OF_RECONNECTION_TO_QUEUE),
                localizer.getMessage(Message.CODE_FOR_CONFIRMATION_OF_RECONNECTION_TO_QUEUE, code)
        );

        delayedJobService.schedule(
                () -> cleanerService.deleteRejoinClientCode(queueId, email),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        return QueueStateForClient.toModel(queueService.getCurrentQueueState(queueId));
    }

    @Override
    public QueueStateForClient confirmCode(Localizer localizer, Long queueId, String email, String code) throws DescriptionException {
        Optional<ClientCodeEntity> clientCode = clientCodeRepo.findById(new ClientCodeEntity.PrimaryKey(queueId, email));
        if (clientCode.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_EXPIRED_PLEASE_TRY_AGAIN));
        }

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_START,
                            email,
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_END
                    )
            );
        }
        ClientCodeEntity clientCodeEntity = clientCode.get();
        if (!Objects.equals(clientCodeEntity.getCode(), code)) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_CODE));
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        clientInQueueEntity.setAccessKey(clientCodeEntity.getCode());
        clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.CONFIRMED.name());
        clientInQueueRepo.save(clientInQueueEntity);
        clientCodeRepo.delete(clientCodeEntity);

        QueueState queueState = queueService.updateCurrentQueueState(queueId);

        return QueueStateForClient.toModel(queueState, clientInQueueEntity);
    }

    @Override
    public QueueStateForClient leaveQueue(Localizer localizer, Long queueId, String email, String accessKey) throws DescriptionException {
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findByQueueIdAndEmail(
                queueId,
                email
        );
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_START,
                            email,
                            Message.CLIENT_WITH_EMAIL_DOES_NOT_STAND_IN_QUEUE_END
                    )
            );
        }

        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (!Objects.equals(clientInQueueEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_LEAVE_QUEUE_PLEASE_TRY_RECONNECT));
        }

        clientInQueueRepo.updateClientsOrderNumberInQueue(queueId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteByEmail(email);

        QueueState queueState = queueService.updateCurrentQueueState(queueId);

        return QueueStateForClient.toModel(queueState);
    }

    private  List<ClientInQueueEntity> checkJoinQueue(Localizer localizer, Long queueId, JoinQueueRequest joinQueueRequest) throws DescriptionException {
        if (joinQueueRequest.getFirstName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (joinQueueRequest.getFirstName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }
        if (joinQueueRequest.getLastName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (joinQueueRequest.getLastName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }
        if (!EmailChecker.emailMatches(joinQueueRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        if (clients.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        if (clientInQueueRepo.existsByQueueIdAndEmail(queueId, joinQueueRequest.getEmail())) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.CLIENT_WITH_EMAIL_STAND_IN_QUEUE_START,
                            joinQueueRequest.getEmail(),
                            Message.CLIENT_WITH_EMAIL_STAND_IN_QUEUE_END
                    )
            );
        }

        return clients.get();
    }
}
