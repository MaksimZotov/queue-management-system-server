package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;

public interface LocationService {
    Location createLocation(String username, CreateLocationRequest createLocationRequest);
    Long deleteLocation(String username, Long queueId);
    Location getLocation(Long queueId, Boolean hasRules);
    ContainerForList<Location> getLocations(String username, Integer page, Integer pageSize, Boolean hasRules);
}
