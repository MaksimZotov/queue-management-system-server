package com.maksimzotov.queuemanagementsystemserver.model.type;

import lombok.Value;

import java.util.List;

@Value
public class QueueTypeModel {
    Long id;
    String name;
    String description;
    List<Long> services;
}
