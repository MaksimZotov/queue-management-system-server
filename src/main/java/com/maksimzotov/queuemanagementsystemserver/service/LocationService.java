package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;

public interface LocationService {
    Location createLocation(String username, CreateLocationRequest createLocationRequest) throws DescriptionException;
    void deleteLocation(String username, Long locationId) throws DescriptionException;
    Location getLocation(Long locationId, String username) throws DescriptionException;
    ContainerForList<Location> getLocations(String username, Boolean hasRules) throws DescriptionException;
}
