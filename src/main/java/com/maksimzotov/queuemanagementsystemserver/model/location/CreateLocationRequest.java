package com.maksimzotov.queuemanagementsystemserver.model.location;

import lombok.Value;

@Value
public class CreateLocationRequest {
    String name;
    String description;
}
