package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepo extends JpaRepository<LocationEntity, Long> {
    List<LocationEntity> findByOwnerEmailContaining(String email);
}
