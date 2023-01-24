package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueClassRepo extends JpaRepository<QueueClassEntity, Long> {

}
