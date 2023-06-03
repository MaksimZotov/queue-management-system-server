package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInServicesSequenceEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInSpecialistEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.service.CreateServiceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.service.OrderedServicesModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
@AllArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final ServiceRepo serviceRepo;
    private final LocationRepo locationRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final SpecialistRepo specialistRepo;
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
    public ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException {
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        return getServicesInSpecialist(localizer, queueEntity.getSpecialistId());
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInSpecialist(Localizer localizer, Long specialistId) throws DescriptionException {
        if (!specialistRepo.existsById(specialistId)) {
            throw new DescriptionException(localizer.getMessage(Message.SPECIALIST_DOES_NOT_EXIST));
        }
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
    public OrderedServicesModel getServicesInServicesSequence(Localizer localizer, Long servicesSequenceId) throws DescriptionException {
        if (!servicesSequenceRepo.existsById(servicesSequenceId)) {
            throw new DescriptionException(localizer.getMessage(Message.SERVICES_SEQUENCE_DOES_NOT_EXIST));
        }
        List<ServiceInServicesSequenceEntity> serviceInServicesSequenceEntities = serviceInServicesSequenceRepo.findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(servicesSequenceId);
        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        for (ServiceInServicesSequenceEntity serviceInServicesSequenceEntity : serviceInServicesSequenceEntities) {
            serviceIdsToOrderNumbers.put(serviceInServicesSequenceEntity.getPrimaryKey().getServiceId(), serviceInServicesSequenceEntity.getOrderNumber());
        }
        return new OrderedServicesModel(serviceIdsToOrderNumbers);
    }

    @Override
    public ServiceModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createServiceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        if (createServiceRequest.getName().isBlank()) {
            throw new DescriptionException(localizer.getMessage(Message.SERVICE_NAME_MUST_NOT_BE_EMPTY));
        }
        return ServiceModel.toModel(
                serviceRepo.save(
                        new ServiceEntity(
                                null,
                                locationId,
                                createServiceRequest.getName(),
                                createServiceRequest.getDescription()
                        )
                )
        );
    }

    @Override
    public void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        if (clientToChosenServiceRepo.existsByPrimaryKeyServiceId(serviceId)) {
            throw new DescriptionException(localizer.getMessage(Message.SERVICE_IS_BOOKED_BY_CLIENT));
        }
        if (serviceInSpecialistRepo.existsByServiceId(serviceId)) {
            throw new DescriptionException(localizer.getMessage(Message.SERVICE_IS_ASSIGNED_TO_SPECIALIST));
        }
        if (serviceInServicesSequenceRepo.existsByPrimaryKeyServiceId(serviceId)) {
            throw new DescriptionException(localizer.getMessage(Message.SERVICE_IS_ASSIGNED_TO_SERVICES_SEQUENCE));
        }
        serviceRepo.deleteById(serviceId);
    }
}
