package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationsOwnerInfo;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.QueueService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
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
    private final RightsService rightsService;
    @Lazy
    private final QueueService queueService;
    private final LocationRepo locationRepo;
    private final RightsRepo rightsRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Location createLocation(Localizer localizer, String accessToken, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);

        if (createLocationRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_NAME_MUST_NOT_BE_EMPTY));
        }

        LocationEntity entity = locationRepo.save(
                new LocationEntity(
                        null,
                        accountEmail,
                        createLocationRequest.getName(),
                        createLocationRequest.getDescription()
                )
        );

        return Location.toModel(entity, true);
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
        locationRepo.deleteById(locationId);
    }

    @Override
    public Location getLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();
        return Location.toModel(
                locationEntity,
                rightsService.checkEmployeeRightsInLocation(accountService.getEmailOrNull(accessToken), locationId)
        );
    }

    @Override
    public ContainerForList<Location> getLocations(Localizer localizer, String accessToken, String email) throws DescriptionException {
        Optional<List<LocationEntity>> locationsEntities = locationRepo.findByOwnerEmailContaining(email);
        if (locationsEntities.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_OWNER_NOT_FOUND));
        }
        return new ContainerForList<>(
                locationsEntities.get()
                        .stream()
                        .map(locationEntity ->
                                Location.toModel(
                                        locationEntity,
                                        Objects.equals(
                                                accountService.getEmailOrNull(accessToken),
                                                locationEntity.getOwnerEmail()
                                        )
                                )
                        )
                        .toList()
        );
    }

    @Override
    public LocationsOwnerInfo checkIsOwner(String accessToken, String email) {
        return new LocationsOwnerInfo(Objects.equals(accountService.getEmailOrNull(accessToken), email));
    }

    @Override
    public BoardModel getLocationBoard(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return BoardModel.toModel(
                clientInQueueRepo,
                queues.get()
        );
    }

    @Override
    public void updateLocationBoard(Long locationId)  {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            return;
        }
        BoardModel boardModel = BoardModel.toModel(
                clientInQueueRepo,
                queues.get()
        );
        messagingTemplate.convertAndSend(
                WebSocketConfig.BOARD_URL + locationId,
                boardModel
        );
    }

    @Override
    public void changeEnabledStateInLocation(Localizer localizer, String accessToken, Long locationId, Boolean enabled) throws DescriptionException, AccountIsNotAuthorizedException {
        Boolean hasRights = rightsService.checkEmployeeRightsInLocation(accountService.getEmail(accessToken), locationId);
        if (!hasRights) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        List<QueueEntity> modifiedQueues = queues
                .get()
                .stream()
                .peek(item -> item.setEnabled(enabled))
                .toList();
        queueRepo.saveAll(modifiedQueues);
        for (QueueEntity entity: modifiedQueues) {
            queueService.updateCurrentQueueState(entity.getId());
        }
    }
}
