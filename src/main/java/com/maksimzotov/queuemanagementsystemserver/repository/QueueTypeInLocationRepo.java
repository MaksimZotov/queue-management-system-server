package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueTypeInLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueTypeInLocationRepo extends JpaRepository<QueueTypeInLocationEntity, QueueTypeInLocationEntity> {
    Optional<List<QueueTypeInLocationEntity>> findAllByLocationId(Long locationId);
    Boolean existsByQueueTypeId(Long queueTypeId);
}
