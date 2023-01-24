package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInQueueClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceInQueueClassRepo extends JpaRepository<ServiceInQueueClassEntity, ServiceInQueueClassEntity> {
    Optional<List<ServiceInQueueClassEntity>> findAllByQueueClassId(Long queueClassId);
}
