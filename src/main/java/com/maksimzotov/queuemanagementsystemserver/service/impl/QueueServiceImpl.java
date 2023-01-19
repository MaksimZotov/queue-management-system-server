package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class QueueServiceImpl implements QueueService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final MailService mailService;
    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;

    @Override
    public Queue createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        if (!rightsService.checkRightsInLocation(accountUsername, locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NAME_MUST_NOT_BE_EMPTY));
        }

        QueueEntity entity = queueRepo.save(
                new QueueEntity(
                        null,
                        locationId,
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription()
                )
        );
        boardService.updateLocation(locationId);

        return Queue.toModel(entity, true);
    }

    @Override
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        clientCodeRepo.deleteByPrimaryKeyQueueId(queueId);
        clientInQueueRepo.deleteByQueueId(queueId);
        queueRepo.deleteById(queueId);
        boardService.updateLocation(queueEntity.getLocationId());
    }

    @Override
    public ContainerForList<Queue> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queuesEntities = queueRepo.findAllByLocationId(locationId);
        if (queuesEntities.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new ContainerForList<>(
                queuesEntities.get().stream()
                        .map((item) -> Queue.toModel(item, rightsService.checkRightsInLocation(accountService.getUsernameOrNull(accessToken), locationId)))
                        .toList()
        );
    }

    @Override
    public QueueState getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
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
                locationEntity.getOwnerUsername()
        );
    }

    @Override
    public void serveClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();

        clientInQueueRepo.updateClientsOrderNumberInQueue(clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteById(clientId);

        updateQueueWithoutTransaction(queueId);
    }

    @Override
    public void notifyClientInQueue(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        String email = clientInQueue.get().getEmail();
        if (email == null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_HAVE_EMAIL));
        }

        mailService.send(email, localizer.getMessage(Message.QUEUE), localizer.getMessage(Message.PLEASE_GO_TO_SERVICE));
    }

    @Override
    public ClientInQueue addClient(Localizer localizer, String accessToken, Long queueId, AddClientRequest addClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        checkAddClient(localizer, addClientRequest);

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<Integer> maxOrderNumber = clientsEntities.stream()
                .map(ClientInQueueEntity::getOrderNumber)
                .max(Integer::compare);

        Integer orderNumber = maxOrderNumber.isEmpty() ? 1 : maxOrderNumber.get() + 1;
        Integer publicCode = CodeGenerator.generate(clientsEntities.stream().map(ClientInQueueEntity::getPublicCode).toList());
        String accessKey = CodeGenerator.generate();

        ClientInQueueEntity clientInQueueEntity = new ClientInQueueEntity(
                null,
                queueId,
                null,
                addClientRequest.getFirstName(),
                addClientRequest.getLastName(),
                orderNumber,
                publicCode,
                accessKey,
                ClientInQueueStatusEntity.Status.CONFIRMED.name()
        );
        clientInQueueRepo.save(clientInQueueEntity);

        updateQueueWithoutTransaction(queueId);

        return ClientInQueue.toModel(clientInQueueEntity);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QueueState getQueueStateWithoutTransaction(Long queueId) {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        QueueEntity queueEntity = queue.get();

        Optional<List<ClientInQueueEntity>> clients = clientInQueueRepo.findAllByQueueId(queueId);
        List<ClientInQueueEntity> clientsEntities = clients.get();

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        return new QueueState(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(ClientInQueue::toModel)
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                locationEntity.getOwnerUsername()
        );
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QueueState updateQueueWithoutTransaction(Long queueId) {
        QueueState queueState = getQueueStateWithoutTransaction(queueId);
        messagingTemplate.convertAndSend(WebSocketConfig.QUEUE_URL + queueId, queueState);
        boardService.updateLocation(queueState.getLocationId());
        return queueState;
    }

    private QueueEntity checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkRightsInLocation(accountUsername, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        return queueEntity;
    }

    private void checkAddClient(Localizer localizer, AddClientRequest addClientRequest) throws DescriptionException {
        if (addClientRequest.getFirstName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (addClientRequest.getFirstName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.FIRST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }
        if (addClientRequest.getLastName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_NOT_BE_EMPTY));
        }
        if (addClientRequest.getLastName().length() > 64) {
            throw new DescriptionException(localizer.getMessage(Message.LAST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS));
        }
    }
}
