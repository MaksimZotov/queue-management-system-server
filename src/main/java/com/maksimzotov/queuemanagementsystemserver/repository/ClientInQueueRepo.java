package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientInQueueRepo extends JpaRepository<ClientInQueueEntity, ClientInQueueEntity.PrimaryKey> {
    Optional<List<ClientInQueueEntity>> findByPrimaryKeyQueueId(Long queueId);

    Boolean existsByPrimaryKeyQueueId(Long queueId);

    @Query("SELECT orderNumber FROM client_in_queue WHERE queue_id = :p_queue_id")
    List<Integer> findOrderNumbersInQueue(@Param("p_queue_id") Long queueId);

    @Modifying
    @Query("UPDATE client_in_queue SET orderNumber = orderNumber - 1 WHERE order_number > :p_order_number")
    void updateClientsOrderNumberInQueue(@Param("p_order_number") Integer orderNumber);

    void deleteByPrimaryKeyEmail(String email);
}
