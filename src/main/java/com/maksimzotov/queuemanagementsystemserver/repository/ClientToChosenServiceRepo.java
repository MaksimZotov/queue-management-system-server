package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientToChosenServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientToChosenServiceRepo extends JpaRepository<ClientToChosenServiceEntity, ClientToChosenServiceEntity.PrimaryKey> {
    List<ClientToChosenServiceEntity> findAllByClientId(Long clientId);
    Boolean existsByPrimaryKeyClientId(Long clientId);
}
