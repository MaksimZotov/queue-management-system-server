package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationsOwnerInfo;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface LocationService {
    Location createLocation(Localizer localizer, String email, CreateLocationRequest createLocationRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    Location getLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    ContainerForList<Location> getLocations(Localizer localizer, String accessToken, String email) throws DescriptionException;
    LocationsOwnerInfo checkIsOwner(String accessToken, String email);
    BoardModel getLocationBoard(Localizer localizer, Long locationId) throws DescriptionException;
    void updateLocationBoard(Long locationId);
    void changeEnabledStateInLocation(Localizer localizer, String accessToken, Long locationId, Boolean enabled) throws DescriptionException, AccountIsNotAuthorizedException;
}
