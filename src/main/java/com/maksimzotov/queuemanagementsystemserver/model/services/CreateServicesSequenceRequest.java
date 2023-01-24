package com.maksimzotov.queuemanagementsystemserver.model.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Map;

@Value
public class CreateServicesSequenceRequest {
    String name;
    String description;
    @JsonProperty("service_ids_to_order_numbers")
    Map<Long, Long> serviceIdsToOrderNumbers;
}
