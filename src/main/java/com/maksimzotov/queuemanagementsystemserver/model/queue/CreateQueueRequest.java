package com.maksimzotov.queuemanagementsystemserver.model.queue;

import lombok.Value;

@Value
public class CreateQueueRequest {
    String name;
    String description;
}