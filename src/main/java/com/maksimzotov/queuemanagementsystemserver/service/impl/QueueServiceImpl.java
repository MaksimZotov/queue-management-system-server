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
    private final SpecialistRepo specialistRepo;
    private final ClientInQueueToChosenServiceRepo clientInQueueToChosenServiceRepo;
    private final ServiceRepo serviceRepo;

    @Override
    public QueueModel createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);

        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountEmail, locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }

        if (createQueueRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_NAME_MUST_NOT_BE_EMPTY));
        }

        if (!specialistRepo.existsByIdAndLocationId(createQueueRequest.getSpecialistId(), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.SPECIALIST_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queueRepo.save(
                new QueueEntity(
                        null,
                        locationId,
                        createQueueRequest.getSpecialistId(),
                        createQueueRequest.getName(),
                        createQueueRequest.getDescription(),
                        true,
                        null
                )
        );
        locationService.updateLocationState(locationId);

        return QueueModel.toModel(queueEntity, true);
    }

    @Override
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        if (clientInQueueRepo.existsByQueueId(queueId)) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_CONTAINS_CLIENTS));
        }
        queueRepo.deleteById(queueId);
        locationService.updateLocationState(queueEntity.getLocationId());
    }

    @Override
    public ContainerForList<QueueModel> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        List<QueueEntity> queuesEntities = queueRepo.findAllByLocationId(locationId);
        return new ContainerForList<>(
                queuesEntities
                        .stream()
                        .map((item) -> QueueModel.toModel(
                                        item,
                                        rightsService.checkEmployeeRightsInLocationNoException(
                                                accountService.getEmailOrNull(accessToken),
                                                locationId
                                        )
                                )
                        )
                        .toList()
        );
    }

    @Override
    public QueueStateModel getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);

        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        return new QueueStateModel(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                locationEntity.getOwnerEmail(),
                queueEntity.getEnabled()
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
                locationEntity.getOwnerEmail(),
                queueEntity.getEnabled()
        );
    }

    @Override
    public void changePausedState(Localizer localizer, String accessToken, Long queueId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        queueEntity.setEnabled(paused);
        queueRepo.save(queueEntity);
        locationService.updateLocationState(queueEntity.getLocationId());
    }

    private QueueEntity checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountEmail, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        return queueEntity;
    }
}
