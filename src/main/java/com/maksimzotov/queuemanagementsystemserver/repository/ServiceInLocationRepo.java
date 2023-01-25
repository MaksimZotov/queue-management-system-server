package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceInLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceInLocationRepo extends JpaRepository<ServiceInLocationEntity, ServiceInLocationEntity> {
}
