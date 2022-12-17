package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface LocationRepo extends CrudRepository<LocationEntity, Long> {
    Page<LocationEntity> findByOwnerUsernameContaining(String ownerUsername, Pageable pageable);
}
