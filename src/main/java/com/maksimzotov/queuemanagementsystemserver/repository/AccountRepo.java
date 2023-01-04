package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepo extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUsername(String username);
    void deleteByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String username);
}
