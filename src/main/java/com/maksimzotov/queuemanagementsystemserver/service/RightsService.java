package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.rights.RightsModel;

public interface RightsService {

    ContainerForList<RightsModel> getRights(String username, Long locationId) throws DescriptionException;
    void addRights(String username, Long locationId, String email) throws DescriptionException;
    void deleteRights(String username, Long locationId, String email) throws DescriptionException;
    public Boolean checkRightsInLocation(String username, Long locationId);
}
