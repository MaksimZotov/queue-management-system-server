package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepo extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByPhone(String phone);
    List<ClientEntity> findAllByLocationId(Long locationId);
    Boolean existsByLocationId(Long locationId);
}
