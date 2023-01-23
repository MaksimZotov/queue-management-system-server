package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RightsRepo extends JpaRepository<RightsEntity, RightsEntity> {
    Optional<List<RightsEntity>> findAllByLocationId(Long locationId);

    void deleteByLocationId(Long locationId);
}
