package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RightsRepo extends JpaRepository<RightsEntity, RightsEntity.PrimaryKey> {
    List<RightsEntity> findAllByPrimaryKeyLocationId(Long locationId);
    Boolean existsByPrimaryKeyAndStatus(RightsEntity.PrimaryKey primaryKey, String status);
    void deleteAllByPrimaryKeyLocationId(Long locationId);
}
