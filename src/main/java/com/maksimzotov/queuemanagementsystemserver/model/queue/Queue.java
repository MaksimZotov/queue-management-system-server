package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Jacksonized
public class Queue {

    private Long id;
    private String name;
    private String description;
    @JsonProperty("has_rules")
    private Boolean hasRules;


    public static Queue toModel(QueueEntity entity, Boolean hasRules) {
        return new Queue(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                hasRules
        );
    }
}
