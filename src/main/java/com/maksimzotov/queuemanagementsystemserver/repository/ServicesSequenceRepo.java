package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesSequenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicesSequenceRepo extends JpaRepository<ServicesSequenceEntity, Long> {
}
