package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCodeRepo extends JpaRepository<ClientCodeEntity, ClientCodeEntity.PrimaryKey> {
    void deleteByPrimaryKeyClientId(Long clientId);
}
