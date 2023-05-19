package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationChange;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationModel;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationState;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final AccountService accountService;
    private final LocationRepo locationRepo;
    private final AccountRepo accountRepo;
    private final RightsRepo rightsRepo;
    private final QueueRepo queueRepo;
    private final ClientRepo clientRepo;
    private final ServiceRepo serviceRepo;
    private final SpecialistRepo specialistRepo;
    private final ServiceInSpecialistRepo serviceInSpecialistRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public LocationModel createLocation(Localizer localizer, String accessToken, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);

        if (createLocationRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_NAME_MUST_NOT_BE_EMPTY));
        }

        LocationEntity locationEntity = locationRepo.save(
                new LocationEntity(
                        null,
                        accountEmail,
                        createLocationRequest.getName(),
                        createLocationRequest.getDescription()
                )
        );

        return LocationModel.toModel(locationEntity);
    }

    @Override
    public void deleteLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);

        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();
        if (!Objects.equals(locationEntity.getOwnerEmail(), accountEmail)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_HAVE_NOT_RIGHTS_TO_DELETE_LOCATION));
        }
        if (clientRepo.existsByLocationId(locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_CONTAINS_CLIENTS));
        }

        rightsRepo.deleteAllByPrimaryKeyLocationId(locationId);
        queueRepo.deleteAllByLocationId(locationId);

        List<ServicesSequenceEntity> servicesSequenceEntities = servicesSequenceRepo.findAllByLocationId(locationId);
        for (ServicesSequenceEntity servicesSequenceEntity : servicesSequenceEntities) {
            serviceInServicesSequenceRepo.deleteAllByPrimaryKeyServicesSequenceId(servicesSequenceEntity.getId());
        }
        servicesSequenceRepo.deleteAllByLocationId(locationId);

        List<SpecialistEntity> specialistEntities = specialistRepo.findAllByLocationId(locationId);
        for (SpecialistEntity specialistEntity : specialistEntities) {
            serviceInSpecialistRepo.deleteAllBySpecialistId(specialistEntity.getId());
        }
        specialistRepo.deleteAllByLocationId(locationId);

        serviceRepo.deleteAllByLocationId(locationId);
        locationRepo.deleteById(locationId);
    }

    @Override
    public LocationModel getLocation(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();
        return LocationModel.toModel(locationEntity);
    }

    @Override
    public ContainerForList<LocationModel> getLocations(Localizer localizer, Long accountId) throws DescriptionException {
        Optional<AccountEntity> account = accountRepo.findById(accountId);
        if (account.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_OWNER_NOT_FOUND));
        }
        AccountEntity accountEntity = account.get();
        List<LocationEntity> locationsEntities = locationRepo.findByOwnerEmailContaining(accountEntity.getEmail());
        return new ContainerForList<>(
                locationsEntities
                        .stream()
                        .map(LocationModel::toModel)
                        .toList()
        );
    }

    @Override
    public LocationState getLocationState(Localizer localizer, Long locationId) {
        List<ClientEntity> clientEntities = clientRepo.findAllByLocationId(locationId);
        List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationId(locationId);
        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyLocationId(locationId);
        List<QueueEntity> queueEntities = queueRepo.findAllByLocationId(locationId);
        return LocationState.toModel(
                locationId,
                clientEntities,
                serviceEntities,
                clientToChosenServiceEntities,
                queueEntities
        );
    }

    @Override
    public void updateLocationState(Long locationId, LocationChange locationChange) {
        messagingTemplate.convertAndSend(
                WebSocketConfig.LOCATION_URL + locationId,
                locationChange
        );
    }
}
