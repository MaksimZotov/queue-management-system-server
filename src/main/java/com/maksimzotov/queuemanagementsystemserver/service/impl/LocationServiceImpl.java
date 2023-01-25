package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.HasRightsInfo;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final AccountService accountService;
    private final RightsService rightsService;
    private final LocationRepo locationRepo;
    private final RightsRepo rightsRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;
    private final ClientRepo clientRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Location createLocation(Localizer localizer, String accessToken, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        String usernameByToken = accountService.getUsername(accessToken);

        if (createLocationRequest.getName().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_NAME_MUST_NOT_BE_EMPTY));
        }

        LocationEntity entity = locationRepo.save(
                new LocationEntity(
                        null,
                        usernameByToken,
                        createLocationRequest.getName(),
                        createLocationRequest.getDescription(),
                        5
                )
        );

        return Location.toModel(entity, true);
    }

    @Override
    public void deleteLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException {
        String usernameByToken = accountService.getUsername(accessToken);

        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();
        if (!Objects.equals(locationEntity.getOwnerUsername(), usernameByToken)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_HAVE_NOT_RIGHTS_TO_DELETE_LOCATION));
        }
        if (clientRepo.existsByLocationId(locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_CONTAINS_CLIENTS));
        }
        rightsRepo.deleteAllByLocationId(locationId);
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
                rightsService.checkRightsInLocation(accountService.getUsernameOrNull(accessToken), locationId)
        );
    }

    @Override
    public ContainerForList<Location> getLocations(Localizer localizer, String accessToken, String username) throws DescriptionException {
        Optional<List<LocationEntity>> locationsEntities = locationRepo.findByOwnerUsernameContaining(username);
        if (locationsEntities.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_OWNER_NOT_FOUND));
        }
        return new ContainerForList<>(
                locationsEntities.get()
                        .stream()
                        .map(item -> Location.toModel(item, checkHasRights(accessToken, username).getHasRights()))
                        .toList()
        );
    }

    @Override
    public HasRightsInfo checkHasRights(String accessToken, String username) {
        return new HasRightsInfo(Objects.equals(accountService.getUsernameOrNull(accessToken), username));
    }

    @Override
    public Location changeMaxColumns(Localizer localizer, String accessToken, Long locationId, Integer maxColumns) throws DescriptionException, AccountIsNotAuthorizedException {
        if (maxColumns < 0) {
            throw new DescriptionException(localizer.getMessage(Message.MAX_COLUMNS_MUST_NOT_BE_LESS_THAN_0));
        }
        if (!rightsService.checkRightsInLocation(accountService.getUsername(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();
        locationEntity.setMaxColumns(maxColumns);
        return Location.toModel(locationRepo.save(locationEntity), true);
    }

    @Override
    public BoardModel getLocationBoard(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = locationRepo.findById(locationId).get();
        return BoardModel.toModel(
                clientInQueueRepo,
                queues.get(),
                locationEntity
        );
    }

    @Override
    public void updateLocationBoard(Long locationId)  {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            return;
        }
        LocationEntity locationEntity = locationRepo.findById(locationId).get();
        BoardModel boardModel = BoardModel.toModel(
                clientInQueueRepo,
                queues.get(),
                locationEntity
        );
        messagingTemplate.convertAndSend(
                WebSocketConfig.BOARD_URL + locationId,
                boardModel
        );
    }
}
