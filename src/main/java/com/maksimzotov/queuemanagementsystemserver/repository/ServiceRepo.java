package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepo extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findAllByLocationId(Long locationId);
    Boolean existsByIdAndLocationId(Long id, Long locationId);
}