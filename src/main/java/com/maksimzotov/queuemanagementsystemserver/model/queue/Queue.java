package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Queue {

    private Long id;
    private String name;
    private String description;
    @JsonProperty("has_rights")
    private Boolean hasRights;
    private Boolean paused;


    public static Queue toModel(QueueEntity entity, Boolean hasRights) {
        return new Queue(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                hasRights,
                entity.getPaused()
        );
    }
}
