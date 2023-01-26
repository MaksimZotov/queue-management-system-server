package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInQueueTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceInQueueTypeRepo extends JpaRepository<ServiceInQueueTypeEntity, ServiceInQueueTypeEntity> {
    List<ServiceInQueueTypeEntity> findAllByQueueTypeId(Long queueTypeId);
    void deleteAllByQueueTypeId(Long queueTypeId);
    List<ServiceInQueueTypeEntity> findAllByServiceId(Long serviceId);
}
