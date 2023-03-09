package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
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
    private final ServiceRepo serviceRepo;
    private final LocationRepo locationRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final QueueRepo queueRepo;

    @Override
    public ContainerForList<ServiceModel> getServicesInLocation(Localizer localizer, Long locationId) throws DescriptionException {
        if (!locationRepo.existsById(locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new ContainerForList<>(serviceRepo.findAllByLocationId(locationId).stream().map(ServiceModel::toModel).toList());
    }

    @Override
    public ServiceModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createServiceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        };
        return ServiceModel.toModel(
                serviceRepo.save(
                        new ServiceEntity(
                                null,
                                locationId,
                                createServiceRequest.getName(),
                                createServiceRequest.getDescription(),
                                true
                        )
                )
        );
    }

    @Override
    public void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        };
        serviceRepo.deleteById(serviceId);
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        return getServicesInSpecialist(localizer, queueEntity.getSpecialistId());
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInSpecialist(Localizer localizer, Long specialistId) {
        List<ServiceInSpecialistEntity> serviceInSpecialistEntities = serviceInSpecialistRepo.findAllBySpecialistId(specialistId);
        List<ServiceModel> serviceModels = new ArrayList<>();
        for (ServiceInSpecialistEntity serviceInSpecialistEntity : serviceInSpecialistEntities) {
            Optional<ServiceEntity> service = serviceRepo.findById(serviceInSpecialistEntity.getServiceId());
            ServiceEntity serviceEntity = service.get();
            serviceModels.add(ServiceModel.toModel(serviceEntity));
        }
        return new ContainerForList<>(serviceModels);
    }

    @Override
    public ContainerForList<ServicesSequenceModel> getServicesSequencesInLocation(Localizer localizer, Long locationId) throws DescriptionException {
        if (!locationRepo.existsById(locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return new ContainerForList<>(servicesSequenceRepo.findAllByLocationId(locationId).stream().map(ServicesSequenceModel::toModel).toList());
    }

    @Override
    public ServicesSequenceModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        };
        ServicesSequenceEntity servicesSequenceEntity = servicesSequenceRepo.save(
                new ServicesSequenceEntity(
                        null,
                        locationId,
                        createServicesSequenceRequest.getName(),
                        createServicesSequenceRequest.getDescription(),
                        true
                )
        );
        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : createServicesSequenceRequest.getServiceIdsToOrderNumbers().entrySet()) {
            if (!serviceRepo.existsByIdAndLocationId(serviceIdToOrderNumber.getKey(), locationId)) {
                throw new DescriptionException(
                        localizer.getMessage(
                                Message.YOU_ARE_TRYING_TO_CREATE_SERVICES_SEQUENCE_WITH_SERVICES_THAT_ARE_NOT_IN_LOCATION
                        )
                );
            }
            serviceInServicesSequenceRepo.save(
                    new ServiceInServicesSequenceEntity(
                            new ServiceInServicesSequenceEntity.PrimaryKey(
                                    serviceIdToOrderNumber.getKey(),
                                    servicesSequenceEntity.getId()
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }
        return ServicesSequenceModel.toModel(servicesSequenceEntity);
    }

    @Override
    public void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        };
        serviceInServicesSequenceRepo.deleteAllByPrimaryKeyServicesSequenceId(servicesSequenceId);
        servicesSequenceRepo.deleteById(servicesSequenceId);
    }
}
