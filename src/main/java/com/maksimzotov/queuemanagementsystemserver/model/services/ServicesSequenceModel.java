package com.maksimzotov.queuemanagementsystemserver.model.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Map;

@Value
public class ServicesSequenceModel {
    String name;
    String description;
    @JsonProperty("service_models_to_order_numbers")
    Map<ServiceModel, Long> serviceModelsToOrderNumbers;
}
