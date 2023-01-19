package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
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

    private final AccountService accountService;
    private final RightsRepo rightsRepo;
    private final LocationRepo locationRepo;
    private final AccountRepo accountRepo;

    @Override
    public ContainerForList<RightsModel> getRights(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        Optional<List<RightsEntity>> rights = rightsRepo.findAllByLocationId(locationId);
        if (rights.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        if (checkRightsInLocation(accountService.getUsernameOrNull(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_VIEW));
        }
        return new ContainerForList<>(rights.get().stream().map(RightsModel::toModel).toList());
    }

    @Override
    @Transactional
    public void addRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsByEmail(localizer, accountService.getUsername(accessToken), locationId, email);
        RightsEntity rightsEntity = new RightsEntity(locationId, email);
        if (rightsRepo.existsById(rightsEntity)) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_START,
                            email,
                            Message.USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_END
                    )
            );
        }
        rightsRepo.save(rightsEntity);
    }

    @Override
    @Transactional
    public void deleteRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsByEmail(localizer, accountService.getUsername(accessToken), locationId, email);
        RightsEntity rightsEntity = new RightsEntity(locationId, email);
        if (!rightsRepo.existsById(rightsEntity)) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_START,
                            email,
                            Message.USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_END
                    )
            );
        }
        rightsRepo.deleteById(rightsEntity);
    }

    @Override
    public Boolean checkRightsInLocation(String username, Long locationId) {
        Optional<AccountEntity> account = accountRepo.findByUsername(username);
        if (account.isEmpty()) {
            return false;
        }
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            return false;
        }
        if (Objects.equals(location.get().getOwnerUsername(), username)) {
            return true;
        }
        return rightsRepo.existsById(new RightsEntity(locationId, account.get().getEmail()));
    }

    private void checkRightsByEmail(Localizer localizer, String username, Long locationId, String email) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }

        Optional<AccountEntity> account = accountRepo.findByEmail(email);
        if (account.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_START,
                            email,
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_END
                    )
            );
        }

        LocationEntity locationEntity = location.get();
        AccountEntity accountEntity = account.get();
        RightsEntity rightsEntityToCheck = new RightsEntity(locationId, accountEntity.getEmail());

        if (!Objects.equals(locationEntity.getOwnerUsername(), username) && !rightsRepo.existsById(rightsEntityToCheck)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
    }
}
