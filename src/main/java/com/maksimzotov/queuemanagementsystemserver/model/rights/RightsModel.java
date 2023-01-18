package com.maksimzotov.queuemanagementsystemserver.model.rights;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsEntity;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class RightsModel {
    @JsonProperty("location_id")
    Long locationId;
    String email;

    public static RightsModel toModel(RightsEntity entity) {
        return new RightsModel(
                entity.getLocationId(),
                entity.getEmail()
        );
    }
}
