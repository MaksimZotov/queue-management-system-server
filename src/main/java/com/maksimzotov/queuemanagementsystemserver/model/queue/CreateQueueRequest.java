package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateQueueRequest {
    @JsonProperty("specialist_id")
    Long specialistId;
    String name;
    String description;
}