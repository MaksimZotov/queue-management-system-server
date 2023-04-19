package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RightsStatusEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueModel {

    private Long id;
    private String name;
    private String description;
    @JsonProperty("has_rights")
    private Boolean hasRights;


    public static QueueModel toModel(QueueEntity entity, Boolean hasRights) {
        return new QueueModel(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                hasRights
        );
    }
}
