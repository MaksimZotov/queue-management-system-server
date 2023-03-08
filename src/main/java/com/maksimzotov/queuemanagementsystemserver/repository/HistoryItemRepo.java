package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.HistoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoryItemRepo extends JpaRepository<HistoryItemEntity, Long> {
    Optional<HistoryItemEntity> findByClientIdAndEndTimeIsNull(Long clientId);
}
