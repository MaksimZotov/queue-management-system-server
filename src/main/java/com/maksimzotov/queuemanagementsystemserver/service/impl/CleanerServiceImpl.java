package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.ClientEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final ClientRepo clientRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;

    @Override
    public void deleteNonConfirmedUser(String email) {
        if (registrationCodeRepo.existsByEmail(email)) {
            registrationCodeRepo.deleteByEmail(email);
            accountRepo.deleteByEmail(email);
        }
    }

    @Override
    public void deleteNonConfirmedClient(Long clientId, String phone) {
        Optional<ClientEntity> client = clientRepo.findByPhone(phone);
        if (client.isEmpty()) {
            return;
        }
        ClientEntity clientEntity = client.get();
        if (!Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.RESERVED.name())) {
            return;
        }
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientRepo.deleteById(clientId);
    }
}
