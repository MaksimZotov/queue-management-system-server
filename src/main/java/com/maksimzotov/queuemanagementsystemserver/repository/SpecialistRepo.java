package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.SpecialistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialistRepo extends JpaRepository<SpecialistEntity, Long> {
    List<SpecialistEntity> findAllByLocationId(Long locationId);
    Boolean existsByIdAndLocationId(Long id, Long locationId);
}
