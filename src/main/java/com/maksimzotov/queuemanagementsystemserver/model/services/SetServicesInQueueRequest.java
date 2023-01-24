package com.maksimzotov.queuemanagementsystemserver.model.services;

import lombok.Value;

import java.util.List;

@Value
public class SetServicesInQueueRequest {
    List<Long> ids;
}
