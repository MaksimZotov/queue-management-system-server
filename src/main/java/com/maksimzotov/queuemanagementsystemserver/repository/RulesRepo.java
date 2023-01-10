package com.maksimzotov.queuemanagementsystemserver.repository;

import com.maksimzotov.queuemanagementsystemserver.entity.RulesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RulesRepo extends JpaRepository<RulesEntity, RulesEntity> {
    Optional<List<RulesEntity>> findAllByLocationId(Long locationId);
}
