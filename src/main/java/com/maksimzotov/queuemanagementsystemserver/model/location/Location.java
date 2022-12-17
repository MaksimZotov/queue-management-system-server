package com.maksimzotov.queuemanagementsystemserver.model.location;

import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {

    private Long id;
    private String name;
    private String description;

    public static Location toModel(LocationEntity entity) {
        return new Location(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
