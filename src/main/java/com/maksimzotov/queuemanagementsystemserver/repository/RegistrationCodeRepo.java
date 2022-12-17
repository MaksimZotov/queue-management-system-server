package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationCodeRepo extends JpaRepository<RegistrationCodeEntity, Long> { }
