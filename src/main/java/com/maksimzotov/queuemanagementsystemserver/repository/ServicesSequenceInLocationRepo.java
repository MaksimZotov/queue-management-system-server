package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesSequenceInLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicesSequenceInLocationRepo extends JpaRepository<ServicesSequenceInLocationEntity, ServicesSequenceInLocationEntity> {
    Optional<List<ServicesSequenceInLocationEntity>> findAllByLocationId(Long locationId);
    Boolean existsByServicesSequenceId(Long servicesSequenceId);
}
