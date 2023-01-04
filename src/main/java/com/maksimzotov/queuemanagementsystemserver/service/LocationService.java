package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;

public interface LocationService {
    Location createLocation(String username, CreateLocationRequest createLocationRequest);
    void deleteLocation(String username, Long queueId) throws DescriptionException;
    Location getLocation(Long queueId, Boolean hasRules) throws DescriptionException;
    ContainerForList<Location> getLocations(String username, Integer page, Integer pageSize, Boolean hasRules);
}
