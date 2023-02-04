package com.maksimzotov.queuemanagementsystemserver.model.specialist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class CreateSpecialistRequest {
    String name;
    String description;
    @JsonProperty("service_ids")
    List<Long> serviceIds;
}
