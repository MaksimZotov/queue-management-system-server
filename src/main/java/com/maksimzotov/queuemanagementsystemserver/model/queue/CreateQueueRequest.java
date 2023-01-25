package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateQueueRequest {
    @JsonProperty("queue_type_id")
    Long queueTypeId;
    String name;
    String description;
}