package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInSpecialistEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.CreateQueueRequest;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueModel;
import com.maksimzotov.queuemanagementsystemserver.model.queue.QueueStateModel;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceInSpecialistRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.SpecialistRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
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
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final SpecialistRepo specialistRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;

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
                        null
                )
        );

        return QueueModel.toModel(queueEntity, true);
    }

    @Override
    public void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        QueueEntity queueEntity = checkRightsInQueue(localizer, accessToken, queueId);
        if (queueEntity.getClientId() != null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_ASSIGNED_TO_QUEUE));
        }
        queueRepo.deleteById(queueId);
    }

    @Override
    public ContainerForList<QueueModel> getQueues(Localizer localizer, String accessToken, Long locationId) {
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
                        .sorted((Comparator.comparing(QueueModel::getId)))
                        .toList()
        );
    }

    @Override
    public QueueStateModel getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();


        Optional<LocationEntity> location = locationRepo.findById(queueEntity.getLocationId());
        LocationEntity locationEntity = location.get();

        List<Long> services = serviceInSpecialistRepo.findAllBySpecialistId(queueEntity.getSpecialistId())
                .stream()
                .map(ServiceInSpecialistEntity::getServiceId)
                .toList();

        return new QueueStateModel(
                queueId,
                queueEntity.getLocationId(),
                queueEntity.getName(),
                queueEntity.getDescription(),
                locationEntity.getOwnerEmail(),
                services
        );
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
