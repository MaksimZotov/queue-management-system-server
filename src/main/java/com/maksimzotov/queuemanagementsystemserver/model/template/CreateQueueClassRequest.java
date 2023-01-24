package com.maksimzotov.queuemanagementsystemserver.model.template;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateQueueClassRequest {
    String name;
    String description;
    @JsonProperty("service_ids")
    List<Long> serviceIds;
}
