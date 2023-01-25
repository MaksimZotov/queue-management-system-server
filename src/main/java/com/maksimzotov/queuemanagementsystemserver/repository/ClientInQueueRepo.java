package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientInQueueRepo extends JpaRepository<ClientInQueueEntity, Long> {
    Optional<List<ClientInQueueEntity>> findAllByQueueId(Long queueId);

    @Modifying
    @Query("UPDATE client_in_queue SET orderNumber = orderNumber - 1 WHERE queue_id = :p_queue_id AND order_number > :p_order_number")
    void updateClientsOrderNumberInQueue(@Param("p_queue_id") Long queueId, @Param("p_order_number") Integer orderNumber);

    Optional<ClientInQueueEntity> findByQueueIdAndClientId(Long queueId, Long clientId);

    Boolean existsByQueueIdAndClientId(Long queueId, Long clientId);

    Boolean existsByQueueId(Long queueId);

    void deleteAllByQueueId(Long queueId);

    void deleteByClientId(Long clientId);
}
