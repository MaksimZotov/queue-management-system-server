package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueTypeEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueTypeInLocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInLocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInQueueTypeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueTypeInLocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueTypeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceInLocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceInQueueTypeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueTypeService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@AllArgsConstructor
public class QueueTypeServiceImpl implements QueueTypeService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final QueueTypeRepo queueTypeRepo;
    private final QueueTypeInLocationRepo queueTypeInLocationRepo;
    private final ServiceInQueueTypeRepo serviceInQueueTypeRepo;
    private final ServiceInLocationRepo serviceInLocationRepo;

    @Override
    public ContainerForList<QueueTypeModel> getQueueTypesInLocation(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<QueueTypeInLocationEntity>> queueTypesInLocation = queueTypeInLocationRepo.findAllByLocationId(locationId);
        if (queueTypesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        List<QueueTypeInLocationEntity> queueTypeInLocationEntities = queueTypesInLocation.get();
        List<QueueTypeModel> queueTypeModels = new ArrayList<>();
        for (QueueTypeInLocationEntity queueTypeInLocationEntity : queueTypeInLocationEntities) {
            Optional<QueueTypeEntity> queueType = queueTypeRepo.findById(queueTypeInLocationEntity.getQueueTypeId());
            if (queueType.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
            QueueTypeEntity queueTypeEntity = queueType.get();
            queueTypeModels.add(QueueTypeModel.toModel(queueTypeEntity));
        }
        return new ContainerForList<>(queueTypeModels);
    }

    @Override
    public QueueTypeModel createQueueTypeInLocation(Localizer localizer, String accessToken, Long locationId, CreateQueueTypeRequest createQueueTypeRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        QueueTypeEntity queueTypeEntity = queueTypeRepo.save(
                new QueueTypeEntity(
                        null,
                        createQueueTypeRequest.getName(),
                        createQueueTypeRequest.getDescription()
                )
        );
        for (Long serviceId : createQueueTypeRequest.getServiceIds()) {
            if (!serviceInLocationRepo.existsById(new ServiceInLocationEntity(serviceId, locationId))) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
            serviceInQueueTypeRepo.save(
                    new ServiceInQueueTypeEntity(
                            serviceId,
                            queueTypeEntity.getId()
                    )
            );
        }
        return QueueTypeModel.toModel(queueTypeEntity);
    }

    @Override
    public void deleteQueueTypeInLocation(Localizer localizer, String accessToken, Long locationId, Long queueTypeId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        Optional<QueueTypeInLocationEntity> queueTypeInLocation = queueTypeInLocationRepo.findById(
                new QueueTypeInLocationEntity(queueTypeId, locationId)
        );
        if (queueTypeInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        QueueTypeInLocationEntity queueTypeInLocationEntity = queueTypeInLocation.get();
        queueTypeInLocationRepo.delete(queueTypeInLocationEntity);
        if (!queueTypeInLocationRepo.existsByQueueTypeId(queueTypeId)) {
            serviceInQueueTypeRepo.deleteAllByQueueTypeId(queueTypeId);
            queueTypeRepo.deleteById(queueTypeId);
        }
    }
}
