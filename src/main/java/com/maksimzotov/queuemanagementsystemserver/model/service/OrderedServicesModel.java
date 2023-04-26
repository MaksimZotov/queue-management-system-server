package com.maksimzotov.queuemanagementsystemserver.model.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Map;

@Value
public class OrderedServicesModel {
    @JsonProperty("service_ids_to_order_numbers")
    Map<Long, Integer> serviceIdsToOrderNumbers;
}
