package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final AccountRepo accountRepo;
    private final LocationRepo locationRepo;

    @Override
    public Location createLocation(
            String username,
            CreateLocationRequest createLocationRequest
    ) {
        LocationEntity entity = locationRepo.save(
                new LocationEntity(
                        createLocationRequest.getName(),
                        createLocationRequest.getDescription(),
                        accountRepo.findByUsername(username)
                )
        );
        return Location.toModel(entity);
    }

    @Override
    public Long deleteLocation(String username, Long id) {
        LocationEntity entity = locationRepo.findById(id).get();
        if (Objects.equals(entity.getOwner().getUsername(), username)) {
            locationRepo.delete(entity);
            return id;
        } else {
            return null;
        }
    }

    @Override
    public ContainerForList<Location> getLocations(String username, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<LocationEntity> pageResult = locationRepo.findByOwnerUsernameContaining(username, pageable);
        return new ContainerForList<>(
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                pageResult.getContent().stream().map(Location::toModel).toList()
        );
    }
}
