package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesInHistoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicesInHistoryItemRepo extends JpaRepository<ServicesInHistoryItemEntity, Long> {
}
