package com.maksimzotov.queuemanagementsystemserver.model.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.RulesEntity;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class RulesModel {
    @JsonProperty("location_id")
    Long locationId;
    String email;

    public static RulesModel toModel(RulesEntity entity) {
        return new RulesModel(
                entity.getLocationId(),
                entity.getEmail()
        );
    }
}
