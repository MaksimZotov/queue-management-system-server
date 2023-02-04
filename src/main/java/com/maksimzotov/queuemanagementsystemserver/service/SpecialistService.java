package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface SpecialistService {
    ContainerForList<SpecialistModel> getQueueTypesInLocation(Localizer localizer, Long locationId) throws DescriptionException;
    SpecialistModel createQueueTypeInLocation(Localizer localizer, String accessToken, Long locationId, CreateSpecialistRequest createSpecialistRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueueTypeInLocation(Localizer localizer, String accessToken, Long locationId, Long queueTypeId) throws DescriptionException, AccountIsNotAuthorizedException;

}
