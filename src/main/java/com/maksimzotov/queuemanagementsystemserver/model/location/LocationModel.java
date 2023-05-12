package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationModel {

    private Long id;
    @JsonProperty("owner_email")
    private String ownerEmail;
    private String name;
    private String description;

    public static LocationModel toModel(LocationEntity entity) {
        return new LocationModel(
                entity.getId(),
                entity.getOwnerEmail(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
