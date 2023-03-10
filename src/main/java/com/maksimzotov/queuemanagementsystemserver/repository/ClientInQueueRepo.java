package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientInQueueRepo extends JpaRepository<ClientInQueueEntity, Long> {

    List<ClientInQueueEntity> findAllByQueueId(Long queueId);

    Optional<ClientInQueueEntity> findByClientId(Long clientId);

    Boolean existsByQueueId(Long queueId);

    void deleteByClientId(Long clientId);
}
