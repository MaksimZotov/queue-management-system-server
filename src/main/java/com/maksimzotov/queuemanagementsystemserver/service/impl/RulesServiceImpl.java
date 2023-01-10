package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RulesEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rules.RulesModel;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RulesRepo;
import com.maksimzotov.queuemanagementsystemserver.service.RulesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class RulesServiceImpl implements RulesService {

    private final RulesRepo rulesRepo;
    private final LocationRepo locationRepo;
    private final AccountRepo accountRepo;

    @Override
    public ContainerForList<RulesModel> getRules(String username, Long locationId) throws DescriptionException {
        Optional<List<RulesEntity>> rules = rulesRepo.findAllByLocationId(locationId);
        if (rules.isEmpty()) {
            throw new DescriptionException("Локации не существует");
        }
        return new ContainerForList<>(rules.get().stream().map(RulesModel::toModel).toList());
    }

    @Override
    @Transactional
    public void addRules(String username, Long locationId, String email) throws DescriptionException {
        check(username, locationId, email);
        RulesEntity rulesEntity = new RulesEntity(locationId, email);
        if (rulesRepo.existsById(rulesEntity)) {
            throw new DescriptionException("У пользователя с почтой " + email + " уже есть права в этой локации");
        }
        rulesRepo.save(rulesEntity);
    }

    @Override
    @Transactional
    public void deleteRules(String username, Long locationId, String email) throws DescriptionException {
        check(username, locationId, email);
        RulesEntity rulesEntity = new RulesEntity(locationId, email);
        if (!rulesRepo.existsById(rulesEntity)) {
            throw new DescriptionException("У пользователя с почтой " + email + " уже нет прав в этой локации");
        }
        rulesRepo.deleteById(rulesEntity);
    }

    public void check(String username, Long locationId, String email) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException("Локации не существует");
        }

        Optional<AccountEntity> account = accountRepo.findByEmail(email);
        if (account.isEmpty()) {
            throw new DescriptionException("Аккаунта с почтой " + email + " не существует");
        }

        LocationEntity locationEntity = location.get();
        AccountEntity accountEntity = account.get();
        RulesEntity rulesEntityToCheck = new RulesEntity(locationId, accountEntity.getEmail());

        if (!Objects.equals(locationEntity.getOwnerUsername(), username) && !rulesRepo.existsById(rulesEntityToCheck)) {
            throw new DescriptionException("У вас нет прав на совершение операции");
        }
    }
}
