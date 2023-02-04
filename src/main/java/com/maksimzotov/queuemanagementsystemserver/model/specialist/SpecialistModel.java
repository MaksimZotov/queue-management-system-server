package com.maksimzotov.queuemanagementsystemserver.model.specialist;

import com.maksimzotov.queuemanagementsystemserver.entity.SpecialistEntity;
import lombok.Value;

@Value
public class SpecialistModel {
    Long id;
    String name;
    String description;

    public static SpecialistModel toModel(SpecialistEntity specialistEntity) {
        return new SpecialistModel(
                specialistEntity.getId(),
                specialistEntity.getName(),
                specialistEntity.getDescription()
        );
    }
}
