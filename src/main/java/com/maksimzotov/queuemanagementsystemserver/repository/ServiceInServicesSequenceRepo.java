package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInServicesSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInServicesSequenceRepo extends JpaRepository<ServiceInServicesSequenceEntity, Long> {
    List<ServiceInServicesSequenceEntity> findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(Long servicesSequenceId);
}
