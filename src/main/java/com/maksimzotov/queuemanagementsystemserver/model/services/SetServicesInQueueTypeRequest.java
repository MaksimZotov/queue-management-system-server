package com.maksimzotov.queuemanagementsystemserver.model.services;

import lombok.Value;

import java.util.List;

@Value
public class SetServicesInQueueTypeRequest {
    List<Long> ids;
}
