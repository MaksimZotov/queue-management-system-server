package com.maksimzotov.queuemanagementsystemserver.model.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateServiceRequest {
    String name;
    String description;
    @JsonProperty("supposed_duration")
    Long supposedDuration;
    @JsonProperty("max_duration")
    Long maxDuration;
}
