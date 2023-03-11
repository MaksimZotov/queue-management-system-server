package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientToChosenServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientToChosenServiceRepo extends JpaRepository<ClientToChosenServiceEntity, ClientToChosenServiceEntity.PrimaryKey> {
    Optional<ClientToChosenServiceEntity> findByPrimaryKeyClientIdAndPrimaryKeyServiceId(Long clientId, Long serviceId);
    List<ClientToChosenServiceEntity> findAllByPrimaryKeyLocationId(Long locationId);
    List<ClientToChosenServiceEntity> findAllByPrimaryKeyClientId(Long clientId);
    Boolean existsByPrimaryKeyClientId(Long clientId);
    Boolean existsByPrimaryKeyServiceId(Long serviceId);
    void deleteByPrimaryKeyClientId(Long clientId);
}
