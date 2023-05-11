package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationCodeRepo extends JpaRepository<RegistrationCodeEntity, String> {
    Optional<RegistrationCodeEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
}
