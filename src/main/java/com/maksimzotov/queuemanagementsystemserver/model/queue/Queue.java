package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Queue {

    private Long id;
    private String name;
    private String description;

    public static Queue toModel(QueueEntity entity) {
        return new Queue(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
