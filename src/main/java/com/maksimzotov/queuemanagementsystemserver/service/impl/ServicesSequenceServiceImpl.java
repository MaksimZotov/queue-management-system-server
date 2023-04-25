package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInServicesSequenceEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ServicesSequenceEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServicesSequenceService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class ServicesSequenceServiceImpl implements ServicesSequenceService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final LocationRepo locationRepo;
    private final ServiceRepo serviceRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;

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
                        createServicesSequenceRequest.getDescription()
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
