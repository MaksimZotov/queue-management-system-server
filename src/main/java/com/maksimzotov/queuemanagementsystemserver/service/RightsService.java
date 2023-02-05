package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.AddRightsRequest;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface RightsService {
    ContainerForList<RightsModel> getRights(Localizer localizer, String email, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    void addRights(Localizer localizer, String accessToken, Long locationId, AddRightsRequest addRightsRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteRights(Localizer localizer, String accessToken, Long locationId, String email) throws DescriptionException, AccountIsNotAuthorizedException;
    String getRightsStatus(Localizer localizer, String email, Long locationId) throws DescriptionException;
    Boolean checkEmployeeRightsInLocation(Localizer localizer, String email, Long locationId) throws DescriptionException;
}
