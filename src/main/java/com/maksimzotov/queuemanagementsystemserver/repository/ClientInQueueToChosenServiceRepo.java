package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueToChosenServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientInQueueToChosenServiceRepo extends JpaRepository<ClientInQueueToChosenServiceEntity, ClientInQueueToChosenServiceEntity> {
    List<ClientInQueueToChosenServiceEntity> findAllByQueueId(Long queueId);
    List<ClientInQueueToChosenServiceEntity> findAllByClientId(Long ClientId);
    void deleteAllByClientId(Long clientId);
}