package com.maksimzotov.queuemanagementsystemserver.model.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ServiceEntity;
import lombok.Value;

@Value
public class ServiceModel {
    Long id;
    String name;
    String description;
    @JsonProperty("supposed_duration")
    Long supposedDuration;
    @JsonProperty("max_duration")
    Long maxDuration;

    public static ServiceModel toModel(ServiceEntity serviceEntity) {
        return new ServiceModel(
                serviceEntity.getId(),
                serviceEntity.getName(),
                serviceEntity.getDescription(),
                serviceEntity.getSupposedDuration(),
                serviceEntity.getMaxDuration()
        );
    }
}
