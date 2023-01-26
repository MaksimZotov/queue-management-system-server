package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesInServicesSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInServicesSequenceRepo extends JpaRepository<ServicesInServicesSequenceEntity, Long> {
    List<ServicesInServicesSequenceEntity> findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(Long servicesSequenceId);
}
