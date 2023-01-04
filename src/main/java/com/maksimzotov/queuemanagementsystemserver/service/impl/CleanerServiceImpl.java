package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final ClientCodeRepo clientCodeRepo;

    @Override
    public void deleteNonActivatedUser(String username) {
        log.info("Checking deletion of user with username {}", username);
        if (registrationCodeRepo.findById(username).isPresent()) {
            registrationCodeRepo.deleteById(username);
            accountRepo.deleteByUsername(username);
            log.info("User with username {} deleted", username);
        }
    }

    @Override
    public void deleteClientCode(Long queueId, String username) {
        ClientCodeEntity.PrimaryKey primaryKey = new ClientCodeEntity.PrimaryKey(
                queueId,
                username
        );
        if (clientCodeRepo.existsById(primaryKey)) {
            clientCodeRepo.deleteById(primaryKey);
        }
    }
}
