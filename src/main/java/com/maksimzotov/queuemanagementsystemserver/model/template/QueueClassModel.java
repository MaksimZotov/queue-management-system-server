package com.maksimzotov.queuemanagementsystemserver.model.template;

import lombok.Value;

import java.util.List;

@Value
public class QueueClassModel {
    Long id;
    String name;
    String description;
    List<Long> services;
}
