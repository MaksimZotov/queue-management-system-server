package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface LocationService {
    LocationModel createLocation(Localizer localizer, String accessToken, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    LocationModel getLocation(Localizer localizer, Long locationId) throws DescriptionException;
    ContainerForList<LocationModel> getLocations(Localizer localizer, Long accountId) throws DescriptionException;
    LocationState getLocationState(Localizer localizer, Long locationId);
    void updateLocationState(Long locationId, LocationChange locationChange);
}
