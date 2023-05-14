package com.maksimzotov.queuemanagementsystemserver.model.service;

import com.maksimzotov.queuemanagementsystemserver.entity.ServiceEntity;
import lombok.Value;

@Value
public class ServiceModel {
    Long id;
    String name;
    String description;

    public static ServiceModel toModel(ServiceEntity serviceEntity) {
        return new ServiceModel(
                serviceEntity.getId(),
                serviceEntity.getName(),
                serviceEntity.getDescription()
        );
    }
}
