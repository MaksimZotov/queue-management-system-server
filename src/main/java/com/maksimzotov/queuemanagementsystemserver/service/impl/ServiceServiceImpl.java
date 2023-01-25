package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.*;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueTypeService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
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
public class ServiceServiceImpl implements ServiceService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final ServiceInLocationRepo serviceInLocationRepo;
    private final ServiceRepo serviceRepo;
    private final ServiceInQueueTypeRepo serviceInQueueTypeRepo;
    private final QueueTypeInLocationRepo queueTypeInLocationRepo;
    private final ServicesSequenceInLocationRepo servicesSequenceInLocationRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final QueueRepo queueRepo;

    @Override
    public ContainerForList<ServiceModel> getServicesInLocation(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<ServiceInLocationEntity>> servicesInLocation = serviceInLocationRepo.findAllByLocationId(locationId);
        if (servicesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        List<ServiceInLocationEntity> serviceInLocationEntities = servicesInLocation.get();
        List<ServiceModel> serviceModels = new ArrayList<>();
        for (ServiceInLocationEntity serviceInLocationEntity : serviceInLocationEntities) {
            Optional<ServiceEntity> service = serviceRepo.findById(serviceInLocationEntity.getServiceId());
            if (service.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
            ServiceEntity serviceEntity = service.get();
            serviceModels.add(ServiceModel.toModel(serviceEntity));
        }
        return new ContainerForList<>(serviceModels);
    }

    @Override
    public ServiceModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createServiceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        ServiceEntity serviceEntity = serviceRepo.save(
                new ServiceEntity(
                        null,
                        createServiceRequest.getName(),
                        createServiceRequest.getDescription(),
                        createServiceRequest.getSupposedDuration(),
                        createServiceRequest.getMaxDuration()
                )
        );
        serviceInLocationRepo.save(
                new ServiceInLocationEntity(
                        serviceEntity.getId(),
                        locationId
                )
        );
        return ServiceModel.toModel(serviceEntity);
    }

    @Override
    public void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        Optional<ServiceInLocationEntity> serviceInLocation = serviceInLocationRepo.findById(
                new ServiceInLocationEntity(serviceId, locationId)
        );
        if (serviceInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        ServiceInLocationEntity serviceInLocationEntity = serviceInLocation.get();
        Optional<List<QueueTypeInLocationEntity>> queueTypesInLocation = queueTypeInLocationRepo.findAllByLocationId(locationId);
        if (queueTypesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        List<QueueTypeInLocationEntity> queueTypeInLocationEntities = queueTypesInLocation.get();
        for (QueueTypeInLocationEntity queueTypeInLocationEntity : queueTypeInLocationEntities) {
            if (serviceInQueueTypeRepo.existsById(
                    new ServiceInQueueTypeEntity(
                            serviceId,
                            queueTypeInLocationEntity.getQueueTypeId()
                    )
            )) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
        }
        serviceInLocationRepo.delete(serviceInLocationEntity);
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        Optional<List<ServiceInQueueTypeEntity>> servicesInQueueType = serviceInQueueTypeRepo.findAllByQueueTypeId(
                queueEntity.getQueueTypeId()
        );
        if (servicesInQueueType.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        List<ServiceInQueueTypeEntity> serviceInQueueTypeEntities = servicesInQueueType.get();
        List<ServiceModel> serviceModels = new ArrayList<>();
        for (ServiceInQueueTypeEntity serviceInQueueTypeEntity : serviceInQueueTypeEntities) {
            Optional<ServiceEntity> service = serviceRepo.findById(serviceInQueueTypeEntity.getServiceId());
            if (service.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
            ServiceEntity serviceEntity = service.get();
            serviceModels.add(ServiceModel.toModel(serviceEntity));
        }
        return new ContainerForList<>(serviceModels);
    }

    @Override
    public ContainerForList<ServicesSequenceModel> getServicesSequencesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<List<ServicesSequenceInLocationEntity>> servicesSequenceInLocation =
                servicesSequenceInLocationRepo.findAllByLocationId(locationId);

        if (servicesSequenceInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        List<ServicesSequenceInLocationEntity> serviceInLocationEntities = servicesSequenceInLocation.get();
        List<ServicesSequenceModel> servicesSequenceModels = new ArrayList<>();
        for (ServicesSequenceInLocationEntity servicesSequenceInLocationEntity : serviceInLocationEntities) {
            Optional<ServicesSequenceEntity> servicesSequence = servicesSequenceRepo.findById(
                    servicesSequenceInLocationEntity.getServicesSequenceId()
            );
            if (servicesSequence.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
            }
            ServicesSequenceEntity servicesSequenceEntity = servicesSequence.get();
            servicesSequenceModels.add(ServicesSequenceModel.toModel(servicesSequenceEntity));
        }
        return new ContainerForList<>(servicesSequenceModels);
    }

    @Override
    public ServicesSequenceModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        ServicesSequenceEntity servicesSequenceEntity = servicesSequenceRepo.save(
                new ServicesSequenceEntity(
                        null,
                        createServicesSequenceRequest.getName(),
                        createServicesSequenceRequest.getDescription()
                )
        );
        servicesSequenceInLocationRepo.save(
                new ServicesSequenceInLocationEntity(
                        servicesSequenceEntity.getId(),
                        locationId
                )
        );
        return ServicesSequenceModel.toModel(servicesSequenceEntity);
    }

    @Override
    public void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        Optional<ServicesSequenceInLocationEntity> servicesSequencesInLocation = servicesSequenceInLocationRepo.findById(
                new ServicesSequenceInLocationEntity(servicesSequenceId, locationId)
        );
        if (servicesSequencesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.ERROR_OCCURRED));
        }
        ServicesSequenceInLocationEntity servicesSequenceInLocationEntity = servicesSequencesInLocation.get();
        servicesSequenceInLocationRepo.delete(servicesSequenceInLocationEntity);
        if (!servicesSequenceInLocationRepo.existsByServicesSequenceId(servicesSequenceId)) {
            servicesSequenceRepo.deleteById(servicesSequenceId);
        }
    }
}
