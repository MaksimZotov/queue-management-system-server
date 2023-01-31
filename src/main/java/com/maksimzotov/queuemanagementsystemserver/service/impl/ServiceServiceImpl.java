package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.services.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.services.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final QueueRepo queueRepo;

    @Override
    public ContainerForList<ServiceModel> getServicesInLocation(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<ServiceInLocationEntity>> servicesInLocation = serviceInLocationRepo.findAllByLocationId(locationId);
        if (servicesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        List<ServiceInLocationEntity> serviceInLocationEntities = servicesInLocation.get();
        List<ServiceModel> serviceModels = new ArrayList<>();
        for (ServiceInLocationEntity serviceInLocationEntity : serviceInLocationEntities) {
            Optional<ServiceEntity> service = serviceRepo.findById(serviceInLocationEntity.getServiceId());
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
            throw new DescriptionException(localizer.getMessage(Message.SERVICE_DOES_NOT_EXIST_IN_LOCATION));
        }
        ServiceInLocationEntity serviceInLocationEntity = serviceInLocation.get();
        Optional<List<QueueTypeInLocationEntity>> queueTypesInLocation = queueTypeInLocationRepo.findAllByLocationId(locationId);
        if (queueTypesInLocation.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        List<QueueTypeInLocationEntity> queueTypeInLocationEntities = queueTypesInLocation.get();
        for (QueueTypeInLocationEntity queueTypeInLocationEntity : queueTypeInLocationEntities) {
            if (serviceInQueueTypeRepo.existsById(
                    new ServiceInQueueTypeEntity(
                            serviceId,
                            queueTypeInLocationEntity.getQueueTypeId()
                    )
            )) {
                throw new DescriptionException(
                        localizer.getMessage(
                                Message.YOU_ARE_TRYING_TO_DELETE_SERVICE_THAT_IS_REFERENCED_BY_OTHER_QUEUE_TYPES
                        )
                );
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
        return getServicesInQueueType(localizer, queueEntity.getQueueTypeId());
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInQueueType(Localizer localizer, Long queueTypeId) {
        List<ServiceInQueueTypeEntity> serviceInQueueTypeEntities = serviceInQueueTypeRepo.findAllByQueueTypeId(queueTypeId);
        List<ServiceModel> serviceModels = new ArrayList<>();
        for (ServiceInQueueTypeEntity serviceInQueueTypeEntity : serviceInQueueTypeEntities) {
            Optional<ServiceEntity> service = serviceRepo.findById(serviceInQueueTypeEntity.getServiceId());
            ServiceEntity serviceEntity = service.get();
            serviceModels.add(ServiceModel.toModel(serviceEntity));
        }
        return new ContainerForList<>(serviceModels);
    }

    @Override
    public ContainerForList<ServicesSequenceModel> getServicesSequencesInLocation(Localizer localizer, Long locationId) {
        Optional<List<ServicesSequenceInLocationEntity>> servicesSequenceInLocation =
                servicesSequenceInLocationRepo.findAllByLocationId(locationId);

        List<ServicesSequenceInLocationEntity> serviceInLocationEntities = servicesSequenceInLocation.get();
        List<ServicesSequenceModel> servicesSequenceModels = new ArrayList<>();
        for (ServicesSequenceInLocationEntity servicesSequenceInLocationEntity : serviceInLocationEntities) {
            Optional<ServicesSequenceEntity> servicesSequence = servicesSequenceRepo.findById(
                    servicesSequenceInLocationEntity.getServicesSequenceId()
            );
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
        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : createServicesSequenceRequest.getServiceIdsToOrderNumbers().entrySet()) {
            if (!serviceInLocationRepo.existsById(new ServiceInLocationEntity(serviceIdToOrderNumber.getKey(), locationId))) {
                throw new DescriptionException(
                        localizer.getMessage(
                                Message.YOU_ARE_TRYING_TO_CREATE_SERVICES_SEQUENCE_WITH_SERVICES_THAT_ARE_NOT_IN_LOCATION
                        )
                );
            }
            serviceInServicesSequenceRepo.save(
                    new ServicesInServicesSequenceEntity(
                            new ServicesInServicesSequenceEntity.PrimaryKey(
                                    servicesSequenceEntity.getId(),
                                    serviceIdToOrderNumber.getKey()
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }
        return ServicesSequenceModel.toModel(servicesSequenceEntity);
    }

    @Override
    public void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId);
        Optional<ServicesSequenceInLocationEntity> servicesSequencesInLocation = servicesSequenceInLocationRepo.findById(
                new ServicesSequenceInLocationEntity(servicesSequenceId, locationId)
        );
        if (servicesSequencesInLocation.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.YOU_ARE_TRYING_TO_DELETE_SERVICES_SEQUENCE_THAT_IS_NOT_IN_LOCATION
                    )
            );
        }
        ServicesSequenceInLocationEntity servicesSequenceInLocationEntity = servicesSequencesInLocation.get();
        servicesSequenceInLocationRepo.delete(servicesSequenceInLocationEntity);
        if (!servicesSequenceInLocationRepo.existsByServicesSequenceId(servicesSequenceId)) {
            servicesSequenceRepo.deleteById(servicesSequenceId);
        }
    }
}
