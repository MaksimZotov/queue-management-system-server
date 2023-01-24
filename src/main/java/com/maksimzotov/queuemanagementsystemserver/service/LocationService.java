package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.HasRightsInfo;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface LocationService {
    Location createLocation(Localizer localizer, String username, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    Location getLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    ContainerForList<Location> getLocations(Localizer localizer, String accessToken, String username) throws DescriptionException;
    HasRightsInfo checkHasRights(String accessToken, String username);
    Location changeMaxColumns(Localizer localizer, String accessToken, Long locationId, Integer maxColumns) throws DescriptionException, AccountIsNotAuthorizedException;
}
