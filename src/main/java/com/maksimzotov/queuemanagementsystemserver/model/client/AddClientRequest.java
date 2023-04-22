package com.maksimzotov.queuemanagementsystemserver.model.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class AddClientRequest {
    String email;
    @JsonProperty("service_ids")
    List<Long> serviceIds;
    @JsonProperty("services_sequence_id")
    Long servicesSequenceId;
    @JsonProperty("confirmation_required")
    Boolean confirmationRequired;
}
