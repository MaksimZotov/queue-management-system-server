package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class LocationsOwnerInfo {
    @JsonProperty("is_owner")
    Boolean isOwner;
}
