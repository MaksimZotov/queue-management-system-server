package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Value
@AllArgsConstructor
public class HasRightsInfo {
    @JsonProperty("has_rights")
    Boolean hasRights;
}
