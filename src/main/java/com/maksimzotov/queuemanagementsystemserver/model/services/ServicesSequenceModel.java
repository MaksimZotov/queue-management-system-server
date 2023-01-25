package com.maksimzotov.queuemanagementsystemserver.model.services;

import com.maksimzotov.queuemanagementsystemserver.entity.ServicesSequenceEntity;
import lombok.Value;

@Value
public class ServicesSequenceModel {
    Long id;
    String name;
    String description;

    public static ServicesSequenceModel toModel(ServicesSequenceEntity servicesSequenceEntity) {
        return new ServicesSequenceModel(
                servicesSequenceEntity.getId(),
                servicesSequenceEntity.getName(),
                servicesSequenceEntity.getDescription()
        );
    }
}
