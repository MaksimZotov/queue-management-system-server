package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientInQueueStatusRepo extends JpaRepository<ClientInQueueStatusEntity, Long> {
    List<ClientInQueueStatusEntity> findByQueueIdContaining(Long queueId);
}
