package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Jacksonized
public class Location {

    private Long id;
    @JsonProperty("owner_username")
    private String ownerUsername;
    private String name;
    private String description;
    @JsonProperty("has_rights")
    private Boolean hasRights;

    public static Location toModel(LocationEntity entity, Boolean hasRights) {
        return new Location(
                entity.getId(),
                entity.getOwnerUsername(),
                entity.getName(),
                entity.getDescription(),
                hasRights
        );
    }
}
