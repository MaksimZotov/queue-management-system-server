package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInSpecialistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInSpecialistRepo extends JpaRepository<ServiceInSpecialistEntity, ServiceInSpecialistEntity> {
    List<ServiceInSpecialistEntity> findAllBySpecialistId(Long queueTypeId);
    void deleteAllBySpecialistId(Long queueTypeId);
    List<ServiceInSpecialistEntity> findAllByServiceId(Long serviceId);
}
