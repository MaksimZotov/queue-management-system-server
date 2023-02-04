package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepo extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmail(String email);
    void deleteByEmail(String email);
    Boolean existsByEmail(String email);
}
