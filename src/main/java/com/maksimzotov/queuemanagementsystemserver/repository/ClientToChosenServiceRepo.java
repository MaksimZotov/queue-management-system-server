package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientToChosenServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientToChosenServiceRepo extends JpaRepository<ClientToChosenServiceEntity, ClientToChosenServiceEntity.PrimaryKey> {
    List<ClientToChosenServiceEntity> findAllByPrimaryKeyLocationId(Long locationId);
    Boolean existsByPrimaryKeyClientId(Long clientId);
    void deleteByPrimaryKeyClientIdAndPrimaryKeyServiceId(Long clientId, Long serviceId);
    void deleteByPrimaryKeyClientId(Long clientId);
}
