package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
public class AddClientRequest {
    @JsonProperty("first_name")
    String firstName;
    @JsonProperty("last_name")
    String lastName;
}