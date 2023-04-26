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
    @JsonProperty("is_owner")
    private Boolean isOwner;
    @JsonProperty("rights_status")
    private String rightsStatus;

    public static LocationModel toModel(LocationEntity entity, Boolean isOwner, String rightsStatus) {
        return new LocationModel(
                entity.getId(),
                entity.getOwnerEmail(),
                entity.getName(),
                entity.getDescription(),
                isOwner,
                rightsStatus
        );
    }
}