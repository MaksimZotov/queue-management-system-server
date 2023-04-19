package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.CreateSpecialistRequest;
import com.maksimzotov.queuemanagementsystemserver.model.specialist.SpecialistModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface SpecialistService {
    ContainerForList<SpecialistModel> getSpecialistsInLocation(Localizer localizer, Long locationId);
    SpecialistModel createSpecialistInLocation(Localizer localizer, String accessToken, Long locationId, CreateSpecialistRequest createSpecialistRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteSpecialistInLocation(Localizer localizer, String accessToken, Long locationId, Long specialistId) throws DescriptionException, AccountIsNotAuthorizedException;

}
