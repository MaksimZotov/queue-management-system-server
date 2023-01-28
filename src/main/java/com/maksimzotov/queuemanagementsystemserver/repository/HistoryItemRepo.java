package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.HistoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryItemRepo extends JpaRepository<HistoryItemEntity, Long> {
}
