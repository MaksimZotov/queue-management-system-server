package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RightsServiceImpl implements RightsService {

    private final RightsRepo rightsRepo;
    private final LocationRepo locationRepo;
    private final AccountRepo accountRepo;

    @Override
    public ContainerForList<RightsModel> getRights(String username, Long locationId) throws DescriptionException {
        Optional<List<RightsEntity>> rights = rightsRepo.findAllByLocationId(locationId);
        if (rights.isEmpty()) {
            throw new DescriptionException("Локации не существует");
        }
        return new ContainerForList<>(rights.get().stream().map(RightsModel::toModel).toList());
    }

    @Override
    @Transactional
    public void addRights(String username, Long locationId, String email) throws DescriptionException {
        checkRightsByEmail(username, locationId, email);
        RightsEntity rightsEntity = new RightsEntity(locationId, email);
        if (rightsRepo.existsById(rightsEntity)) {
            throw new DescriptionException("У пользователя с почтой " + email + " уже есть права в этой локации");
        }
        rightsRepo.save(rightsEntity);
    }

    @Override
    @Transactional
    public void deleteRights(String username, Long locationId, String email) throws DescriptionException {
        checkRightsByEmail(username, locationId, email);
        RightsEntity rightsEntity = new RightsEntity(locationId, email);
        if (!rightsRepo.existsById(rightsEntity)) {
            throw new DescriptionException("У пользователя с почтой " + email + " уже нет прав в этой локации");
        }
        rightsRepo.deleteById(rightsEntity);
    }

    public void checkRightsByEmail(String username, Long locationId, String email) throws DescriptionException {
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
        RightsEntity rightsEntityToCheck = new RightsEntity(locationId, accountEntity.getEmail());

        if (!Objects.equals(locationEntity.getOwnerUsername(), username) && !rightsRepo.existsById(rightsEntityToCheck)) {
            throw new DescriptionException("У вас нет прав на совершение операции");
        }
    }

    public Boolean checkRightsInLocation(String username, Long locationId) {
        Optional<AccountEntity> account = accountRepo.findByUsername(username);
        if (account.isEmpty()) {
            return false;
        }
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            return  false;
        }
        if (Objects.equals(location.get().getOwnerUsername(), username)) {
            return true;
        }
        return rightsRepo.existsById(new RightsEntity(locationId, account.get().getEmail()));
    }
}
