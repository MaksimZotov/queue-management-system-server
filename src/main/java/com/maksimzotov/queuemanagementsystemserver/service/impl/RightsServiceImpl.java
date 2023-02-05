package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.repository.LocationRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RightsRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class RightsServiceImpl implements RightsService {

    enum RequestType {
        CHANGE,
        VIEW
    }

    private final AccountService accountService;
    private final RightsRepo rightsRepo;
    private final LocationRepo locationRepo;

    @Override
    public ContainerForList<RightsModel> getRights(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkAdministratorRightsByEmail(localizer, accountService.getEmail(accessToken), locationId, RequestType.VIEW);
        List<RightsEntity> rightsEntities = rightsRepo.findAllByPrimaryKeyLocationId(locationId);
        return new ContainerForList<>(rightsEntities.stream().map(RightsModel::toModel).toList());
    }

    @Override
    public void addRights(Localizer localizer, String accessToken, Long locationId, AddRightsRequest addRightsRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        checkAdministratorRightsByEmail(localizer, accountService.getEmail(accessToken), locationId, RequestType.CHANGE);
        if (!EmailChecker.emailMatches(addRightsRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_EMAIL));
        }
        RightsEntity.PrimaryKey primaryKey = new RightsEntity.PrimaryKey(locationId, addRightsRequest.getEmail());
        if (rightsRepo.existsById(primaryKey)) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_START,
                            addRightsRequest.getEmail(),
                            Message.USER_WITH_EMAIL_HAS_RIGHTS_IN_LOCATION_END
                    )
            );
        }
        rightsRepo.save(new RightsEntity(primaryKey, addRightsRequest.getStatus()));
    }

    @Override
    public void deleteRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException {
        checkAdministratorRightsByEmail(localizer, accountService.getEmail(accessToken), locationId, RequestType.CHANGE);
        RightsEntity.PrimaryKey primaryKey = new RightsEntity.PrimaryKey(locationId, email);
        if (!rightsRepo.existsById(primaryKey)) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_START,
                            email,
                            Message.USER_WITH_EMAIL_DOES_NOT_HAVE_RIGHTS_IN_LOCATION_END
                    )
            );
        }
        rightsRepo.deleteById(primaryKey);
    }

    @Override
    public String getRightsStatus(Localizer localizer, String email, Long locationId) throws DescriptionException {
        if (!locationRepo.existsById(locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        RightsEntity.PrimaryKey primaryKey = new RightsEntity.PrimaryKey(locationId, email);
        Optional<RightsEntity> rights = rightsRepo.findById(primaryKey);
        if (rights.isEmpty()) {
            return null;
        }
        return rights.get().getStatus();
    }

    @Override
    public Boolean checkEmployeeRightsInLocation(Localizer localizer, String email, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        if (Objects.equals(location.get().getOwnerEmail(), email)) {
            return true;
        }
        return rightsRepo.existsById(new RightsEntity.PrimaryKey(locationId, email));
    }

    @Override
    public Boolean checkEmployeeRightsInLocationNoException(String email, Long locationId) {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            return false;
        }
        if (Objects.equals(location.get().getOwnerEmail(), email)) {
            return true;
        }
        return rightsRepo.existsById(new RightsEntity.PrimaryKey(locationId, email));
    }

    private void checkAdministratorRightsByEmail(Localizer localizer, String accountEmail, Long locationId, RequestType requestType) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        LocationEntity locationEntity = location.get();

        if (Objects.equals(locationEntity.getOwnerEmail(), accountEmail)) {
            return;
        }

        RightsEntity.PrimaryKey primaryKey = new RightsEntity.PrimaryKey(locationId, accountEmail);
        if (!rightsRepo.existsByPrimaryKeyAndStatus(primaryKey, RightsStatusEntity.Status.ADMINISTRATOR.name())) {
            throw new DescriptionException(
                    localizer.getMessage(
                            switch (requestType) {
                                case CHANGE -> Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION;
                                case VIEW -> Message.YOU_DO_NOT_HAVE_RIGHTS_TO_VIEW;
                            }
                    )
            );
        }
    }
}
