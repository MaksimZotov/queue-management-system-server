package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicesSequenceRepo extends JpaRepository<ServicesSequenceEntity, Long> {
    List<ServicesSequenceEntity> findAllByLocationId(Long locationId);
    Boolean existsByIdAndLocationId(Long id, Long locationId);
}
