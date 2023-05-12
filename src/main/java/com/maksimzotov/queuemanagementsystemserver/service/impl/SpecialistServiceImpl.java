package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInSpecialistEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.SpecialistEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceInSpecialistRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ServiceRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.SpecialistRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.SpecialistService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@AllArgsConstructor
public class SpecialistServiceImpl implements SpecialistService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final SpecialistRepo specialistRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;
    private final ServiceRepo serviceRepo;
    private final QueueRepo queueRepo;

    @Override
    public ContainerForList<SpecialistModel> getSpecialistsInLocation(Localizer localizer, Long locationId) {
        return new ContainerForList<>(specialistRepo.findAllByLocationId(locationId).stream().map(SpecialistModel::toModel).toList());
    }

    @Override
    public SpecialistModel createSpecialistInLocation(Localizer localizer, String accessToken, Long locationId, CreateSpecialistRequest createSpecialistRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId);
        SpecialistEntity specialistEntity = specialistRepo.save(
                new SpecialistEntity(
                        null,
                        locationId,
                        createSpecialistRequest.getName(),
                        createSpecialistRequest.getDescription()
                )
        );
        for (Long serviceId : createSpecialistRequest.getServiceIds()) {
            if (!serviceRepo.existsByIdAndLocationId(serviceId, locationId)) {
                throw new DescriptionException(
                        localizer.getMessage(
                                Message.YOU_ARE_TRYING_TO_CREATE_SPECIALIST_WITH_SERVICES_THAT_ARE_NOT_IN_LOCATION
                        )
                );
            }
            serviceInSpecialistRepo.save(
                    new ServiceInSpecialistEntity(
                            serviceId,
                            specialistEntity.getId()
                    )
            );
        }
        return SpecialistModel.toModel(specialistEntity);
    }

    @Override
    public void deleteSpecialistInLocation(Localizer localizer, String accessToken, Long locationId, Long specialistId) throws DescriptionException, AccountIsNotAuthorizedException {
        rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId);
        if (queueRepo.existsBySpecialistId(specialistId)) {
            throw new DescriptionException(localizer.getMessage(Message.CREATED_FROM_SPECIALIST_QUEUE_EXIST));
        }
        serviceInSpecialistRepo.deleteAllBySpecialistId(specialistId);
        specialistRepo.deleteById(specialistId);
    }
}
