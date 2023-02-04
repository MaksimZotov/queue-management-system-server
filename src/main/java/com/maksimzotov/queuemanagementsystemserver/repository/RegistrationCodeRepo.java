package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RegistrationCodeRepo extends JpaRepository<RegistrationCodeEntity, String> {
    Boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
