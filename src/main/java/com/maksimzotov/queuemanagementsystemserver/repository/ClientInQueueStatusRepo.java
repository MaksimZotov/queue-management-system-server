package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientInQueueStatusRepo extends JpaRepository<ClientInQueueStatusEntity, Long> {
    List<ClientInQueueStatusEntity> findByQueueId(Long queueId);

    @Query("SELECT clientOrderNumber FROM client_in_queue_status WHERE queue_id = :p_queue_id")
    List<Integer> findOrderNumbersInQueue(@Param("p_queue_id") Long queueId);

    @Modifying
    @Query("UPDATE client_in_queue_status SET clientOrderNumber = clientOrderNumber - 1 WHERE queue_id = :p_queue_id")
    void updateClientsOrderNumberInQueue(@Param("p_queue_id") Long queueId);
}
