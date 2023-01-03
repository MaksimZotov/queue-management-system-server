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
    private String name;
    private String description;
    @JsonProperty("has_rules")
    private Boolean hasRules;

    public static Location toModel(LocationEntity entity, Boolean hasRules) {
        return new Location(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                hasRules
        );
    }
}
