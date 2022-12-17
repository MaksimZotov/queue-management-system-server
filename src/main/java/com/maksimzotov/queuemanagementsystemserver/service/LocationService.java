package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.location.CreateLocationRequest;
import com.maksimzotov.queuemanagementsystemserver.model.location.Location;

public interface LocationService {
    Location createLocation(String username, CreateLocationRequest createLocationRequest);
    Long deleteLocation(String username, Long id);
    ContainerForList<Location> getLocations(String username, Integer page, Integer pageSize);
}
