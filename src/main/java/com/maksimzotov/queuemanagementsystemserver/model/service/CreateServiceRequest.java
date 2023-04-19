package com.maksimzotov.queuemanagementsystemserver.model.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateServiceRequest {
    String name;
    String description;
}
