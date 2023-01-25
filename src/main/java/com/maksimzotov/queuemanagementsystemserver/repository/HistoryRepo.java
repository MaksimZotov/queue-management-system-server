package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepo extends JpaRepository<HistoryEntity, Long> {
}
