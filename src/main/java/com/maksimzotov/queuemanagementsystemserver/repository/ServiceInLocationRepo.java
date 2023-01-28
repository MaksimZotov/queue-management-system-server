package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceInLocationRepo extends JpaRepository<ServiceInLocationEntity, ServiceInLocationEntity> {
    Optional<List<ServiceInLocationEntity>> findAllByLocationId(Long locationId);
}
