package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.model.services.SetServicesInQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class QueueServiceImpl implements QueueService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final MailService mailService;
    private final BoardService boardService;
    private final ServiceService serviceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientCodeRepo clientCodeRepo;
    private final ServiceInQueueTypeRepo serviceInQueueTypeRepo;
    private final QueueTypeInLocationRepo queueTypeInLocationRepo;

    @Override
    public Queue createQueue(Localizer localizer, String accessToken, Long locationId, Long queueTypeId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        if (!rightsService.checkRightsInLocation(accountUsername, locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NAME_MUST_NOT_BE_EMPTY));
        }

        QueueEntity queueEntity = queueRepo.save(
                new QueueEntity(
                        null,
                        locationId,
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription(),
                        true
                )
        );
        if (queueTypeId != null) {
            if (!queueTypeInLocationRepo.existsById(new QueueTypeInLocationEntity(queueTypeId, locationId))) {
                throw new DescriptionException(localizer.getMessage(Message.QUEUE_CLASS_DOES_NOT_EXIST));
            }
            Optional<List<ServiceInQueueTypeEntity>> services = serviceInQueueTypeRepo.findAllByQueueTypeId(queueTypeId);
            serviceService.setServicesInQueue(
                    localizer,
                    accessToken,
                    queueEntity.getId(),
                    new SetServicesInQueueRequest(
                            services.get().stream().map(ServiceInQueueTypeEntity::getServiceId).toList()
                    )
            );
        }
        boardService.updateLocationBoard(locationId);

        return Queue.toModel(queueEntity, true);
    }

    @Override
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        clientCodeRepo.deleteByPrimaryKeyQueueId(queueId);
        clientInQueueRepo.deleteByQueueId(queueId);
        queueRepo.deleteById(queueId);
        boardService.updateLocationBoard(queueEntity.getLocationId());
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
                locationEntity.getOwnerUsername(),
                queueEntity.getPaused()
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

        clientInQueueRepo.updateClientsOrderNumberInQueue(queueId, clientInQueueEntity.getOrderNumber());
        clientInQueueRepo.deleteById(clientId);

        updateCurrentQueueState(queueId);
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

        updateCurrentQueueState(queueId);

        return ClientInQueue.toModel(clientInQueueEntity);
    }

    @Override
    public QueueState getCurrentQueueState(Long queueId) {
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
                locationEntity.getOwnerUsername(),
                queueEntity.getPaused()
        );
    }

    @Override
    public QueueState updateCurrentQueueState(Long queueId) {
        QueueState queueState = getCurrentQueueState(queueId);
        messagingTemplate.convertAndSend(WebSocketConfig.QUEUE_URL + queueId, queueState);
        boardService.updateLocationBoard(queueState.getLocationId());
        return queueState;
    }

    @Override
    public void changePausedState(Localizer localizer, String accessToken, Long queueId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        queueEntity.setPaused(paused);
        queueRepo.save(queueEntity);
        updateCurrentQueueState(queueId);
    }

    @Override
    public void changePausedStateInLocation(Localizer localizer, String accessToken, Long locationId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException {
        Boolean hasRights = rightsService.checkRightsInLocation(
                accountService.getUsername(accessToken),
                locationId
        );
        if (!hasRights) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        List<QueueEntity> modifiedQueues = queues
                .get()
                .stream()
                .peek(item -> item.setPaused(paused))
                .toList();
        queueRepo.saveAll(modifiedQueues);
        for (QueueEntity entity: modifiedQueues) {
            updateCurrentQueueState(entity.getId());
        }
    }

    @Override
    public void switchClientLateState(Localizer localizer, String accessToken, Long queueId, Long clientId, Boolean late) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        Optional<ClientInQueueEntity> clientInQueue = clientInQueueRepo.findById(clientId);
        if (clientInQueue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_STAND_IN_QUEUE));
        }
        ClientInQueueEntity clientInQueueEntity = clientInQueue.get();
        if (Objects.equals(clientInQueueEntity.getStatus(), ClientInQueueStatusEntity.Status.RESERVED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.WAIT_FOR_CONFIRMATION_OF_CODE_BY_CLIENT));
        }
        if (late) {
            clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.LATE.name());
        } else {
            clientInQueueEntity.setStatus(ClientInQueueStatusEntity.Status.CONFIRMED.name());
        }
        updateCurrentQueueState(queueId);
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
