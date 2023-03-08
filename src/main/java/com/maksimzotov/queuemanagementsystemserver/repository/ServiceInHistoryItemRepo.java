package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInHistoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInHistoryItemRepo extends JpaRepository<ServiceInHistoryItemEntity, Long> {
}
