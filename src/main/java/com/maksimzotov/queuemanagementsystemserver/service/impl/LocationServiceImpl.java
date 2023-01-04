package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final AccountRepo accountRepo;
    private final LocationRepo locationRepo;

    @Override
    public Location createLocation(String username, CreateLocationRequest createLocationRequest) {
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
    public void deleteLocation(String username, Long queueId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(queueId);
        if (location.isEmpty()) {
            throw new DescriptionException("Location does not exist");
        }
        LocationEntity locationEntity = location.get();
        Optional<AccountEntity> account = accountRepo.findByUsername(locationEntity.getOwnerUsername());
        if (account.isEmpty()) {
            throw new DescriptionException("Location owner does not exist");
        }
        AccountEntity accountEntity = account.get();
        if (Objects.equals(accountEntity.getUsername(), username)) {
            locationRepo.deleteById(queueId);
        } else {
            throw new DescriptionException("You are not an owner of this location");
        }
    }

    @Override
    public Location getLocation(Long queueId, Boolean hasRules) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(queueId);
        if (location.isEmpty()) {
            throw new DescriptionException("Location does not exist");
        }
        LocationEntity locationEntity = location.get();
        return Location.toModel(locationEntity, hasRules);
    }

    @Override
    public ContainerForList<Location> getLocations(String username, Integer page, Integer pageSize, Boolean hasRules) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<LocationEntity> pageResult = locationRepo.findByOwnerUsernameContaining(username, pageable);
        return new ContainerForList<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.getContent().stream().map((item) -> Location.toModel(item, hasRules)).toList()
        );
    }
}
