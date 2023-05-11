package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepo extends JpaRepository<ClientEntity, Long> {

    @Query(
            value = "SELECT code FROM client WHERE location_id = :locationId AND code IS NOT NULL",
            nativeQuery = true
    )
    List<Integer> findAllClientCodesInLocation(@Param("locationId") Long locationId);

    Optional<ClientEntity> findByPhone(String phone);
    List<ClientEntity> findAllByLocationId(Long locationId);
    Boolean existsByLocationId(Long locationId);
}
