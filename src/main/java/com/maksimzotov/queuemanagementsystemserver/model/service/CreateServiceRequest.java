package com.maksimzotov.queuemanagementsystemserver.model.service;

import lombok.Value;

@Value
public class CreateServiceRequest {
    String name;
    String description;
}
