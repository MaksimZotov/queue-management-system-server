package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final QueueTypeInLocationRepo queueTypeInLocationRepo;

    @Override
    public QueueModel createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountUsername = accountService.getUsername(accessToken);

        if (!rightsService.checkRightsInLocation(accountUsername, locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NAME_MUST_NOT_BE_EMPTY));
        }

        if (!queueTypeInLocationRepo.existsById(new QueueTypeInLocationEntity(createQueueRequest.getQueueTypeId(), locationId))) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_TYPE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queueRepo.save(
                new QueueEntity(
                        null,
                        createQueueRequest.getQueueTypeId(),
                        locationId,
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription(),
                        true
                )
        );
        locationService.updateLocationBoard(locationId);

        return QueueModel.toModel(queueEntity, true);
    }

    @Override
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        if (clientInQueueRepo.existsByQueueId(queueId)) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_CONTAINS_CLIENTS));
        }
        queueRepo.deleteById(queueId);
        locationService.updateLocationBoard(queueEntity.getLocationId());
    }

    @Override
    public ContainerForList<QueueModel> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queuesEntities = queueRepo.findAllByLocationId(locationId);
        if (queuesEntities.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new ContainerForList<>(
                queuesEntities.get().stream()
                        .map((item) -> QueueModel.toModel(item, rightsService.checkRightsInLocation(accountService.getUsernameOrNull(accessToken), locationId)))
                        .toList()
        );
    }

    @Override
    public QueueStateModel getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        List<ClientInQueueEntity> clientsEntities = clientInQueueRepo.findAllByQueueId(queueId);

        return new QueueStateModel(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(clientInQueueEntity -> {
                            ClientEntity clientEntity = clientRepo.findById(clientInQueueEntity.getClientId()).get();
                            return ClientInQueue.toModel(clientInQueueEntity, clientEntity);
                        })
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                locationEntity.getOwnerUsername(),
                queueEntity.getPaused()
        );
    }

    @Override
    public QueueStateModel getCurrentQueueState(Long queueId) {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        QueueEntity queueEntity = queue.get();

        List<ClientInQueueEntity> clientsEntities = clientInQueueRepo.findAllByQueueId(queueId);

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        return new QueueStateModel(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                clientsEntities.stream()
                        .map(clientInQueueEntity -> {
                            ClientEntity clientEntity = clientRepo.findById(clientInQueueEntity.getClientId()).get();
                            return ClientInQueue.toModel(clientInQueueEntity, clientEntity);
                        })
                        .sorted(Comparator.comparingInt(ClientInQueue::getOrderNumber))
                        .toList(),
                locationEntity.getOwnerUsername(),
                queueEntity.getPaused()
        );
    }

    @Override
    public QueueStateModel updateCurrentQueueState(Long queueId) {
        QueueStateModel queueStateModel = getCurrentQueueState(queueId);
        messagingTemplate.convertAndSend(WebSocketConfig.QUEUE_URL + queueId, queueStateModel);
        locationService.updateLocationBoard(queueStateModel.getLocationId());
        return queueStateModel;
    }

    @Override
    public void changePausedState(Localizer localizer, String accessToken, Long queueId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        queueEntity.setPaused(paused);
        queueRepo.save(queueEntity);
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
}
