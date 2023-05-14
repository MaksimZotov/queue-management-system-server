package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueRepo extends JpaRepository<QueueEntity, Long> {
    List<QueueEntity> findAllByLocationId(Long locationId);
    Optional<QueueEntity> findByClientId(Long clientId);
    Boolean existsBySpecialistId(Long specialistId);
    void deleteAllByLocationId(Long locationId);
}
