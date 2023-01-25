package com.maksimzotov.queuemanagementsystemserver.model.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class CreateQueueTypeRequest {
    String name;
    String description;
    @JsonProperty("service_ids")
    List<Long> serviceIds;
}
