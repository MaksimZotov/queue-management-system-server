package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ChangeClientRequest {
    @JsonProperty("service_ids_to_order_numbers")
    Map<Long, Integer> serviceIdsToOrderNumbers;
}