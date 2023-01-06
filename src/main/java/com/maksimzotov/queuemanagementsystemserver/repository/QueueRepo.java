package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueRepo extends JpaRepository<QueueEntity, Long> {
    Page<QueueEntity> findByLocationId(Long locationId, Pageable pageable);
    Optional<Iterable<Long>> findAllIdByLocationId(Long locationId);
    void deleteByLocationId(Long locationId);
}
