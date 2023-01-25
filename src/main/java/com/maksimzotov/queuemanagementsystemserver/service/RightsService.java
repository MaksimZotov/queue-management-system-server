package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface RightsService {
    ContainerForList<RightsModel> getRights(Localizer localizer, String username, Long locationId) throws DescriptionException;
    void addRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException;
    Boolean checkRightsInLocation(String username, Long locationId);
}
