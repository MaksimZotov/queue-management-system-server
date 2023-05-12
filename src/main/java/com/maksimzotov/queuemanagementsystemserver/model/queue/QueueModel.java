package com.maksimzotov.queuemanagementsystemserver.model.queue;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueModel {

    private Long id;
    private String name;
    private String description;


    public static QueueModel toModel(QueueEntity entity) {
        return new QueueModel(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
