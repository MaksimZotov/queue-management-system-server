package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientToChosenServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientToChosenServiceRepo extends JpaRepository<ClientToChosenServiceEntity, ClientToChosenServiceEntity.PrimaryKey> {
    List<ClientToChosenServiceEntity> findAllByPrimaryKeyClientId(Long clientId);
    List<ClientToChosenServiceEntity> findAllByPrimaryKeyLocationId(Long locationId);
    Boolean existsByPrimaryKeyClientId(Long clientId);
    void deleteByPrimaryKeyServiceId(Long serviceId);
    void deleteByPrimaryKeyClientId(Long clientId);
}
