package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueTypeRepo extends JpaRepository<QueueTypeEntity, Long> {

}
