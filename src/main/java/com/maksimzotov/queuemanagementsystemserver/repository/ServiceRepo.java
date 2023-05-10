package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepo extends JpaRepository<ServiceEntity, Long> {

    @Query(
            value = "SELECT * FROM service LEFT JOIN client_to_chosen_service " +
                    "ON service.id = client_to_chosen_service.service_id " +
                    "WHERE service.location_id = :locationId AND client_to_chosen_service.client_id = :clientId",
            nativeQuery = true
    )
    List<ServiceEntity> findAllByLocationIdAndAssignedToClient(
            @Param("locationId") Long locationId,
            @Param("clientId") Long clientId
    );

    List<ServiceEntity> findAllByLocationId(Long locationId);
    Boolean existsByIdAndLocationId(Long id, Long locationId);
}