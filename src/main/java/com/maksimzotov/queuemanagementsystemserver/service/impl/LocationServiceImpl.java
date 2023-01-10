package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.RulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final RulesService rulesService;
    private final LocationRepo locationRepo;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;

    private final ClientCodeRepo clientCodeRepo;

    @Override
    public Location createLocation(String username, CreateLocationRequest createLocationRequest) throws DescriptionException {
        if (createLocationRequest.getName().isEmpty()) {
            throw new DescriptionException("Название локации не может быть пустым");
        }
        LocationEntity entity = locationRepo.save(
                new LocationEntity(
                        null,
                        username,
                        createLocationRequest.getName(),
                        createLocationRequest.getDescription()

                )
        );
        return Location.toModel(entity, true);
    }

    @Override
    public void deleteLocation(String username, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException("Локации не существует");
        }
        LocationEntity locationEntity = location.get();
        if (!Objects.equals(locationEntity.getOwnerUsername(), username)) {
            throw new DescriptionException("У вас нет прав на удаление локации");

        }
        Optional<List<QueueEntity>> queueEntities = queueRepo.findAllByLocationId(locationId);
        if (queueEntities.isEmpty()) {
            throw new IllegalStateException("Failed when fetching queues ids by location id");
        }
        for (QueueEntity queueEntity : queueEntities.get()) {
            clientCodeRepo.deleteByPrimaryKeyQueueId(queueEntity.getId());
            clientInQueueRepo.deleteByQueueId(queueEntity.getId());
        }
        queueRepo.deleteByLocationId(locationId);
        locationRepo.deleteById(locationId);
    }

    @Override
    public Location getLocation(Long locationId, String username) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException("Локации не сущестует");
        }
        LocationEntity locationEntity = location.get();
        return Location.toModel(locationEntity, rulesService.checkRulesInLocation(username, locationId));
    }

    @Override
    public ContainerForList<Location> getLocations(String username, Boolean hasRules) throws DescriptionException {
        Optional<List<LocationEntity>> locationsEntities = locationRepo.findByOwnerUsernameContaining(username);
        if (locationsEntities.isEmpty()) {
            throw new DescriptionException("Владелец локаций не найден");
        }
        return new ContainerForList<>(
                locationsEntities.get().stream().map((item) -> Location.toModel(item, hasRules)).toList()
        );
    }
}
