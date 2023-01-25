package com.maksimzotov.queuemanagementsystemserver.model.type;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueTypeEntity;
import lombok.Value;

import java.util.List;

@Value
public class QueueTypeModel {
    Long id;
    String name;
    String description;

    public static QueueTypeModel toModel(QueueTypeEntity queueTypeEntity) {
        return new QueueTypeModel(
                queueTypeEntity.getId(),
                queueTypeEntity.getName(),
                queueTypeEntity.getDescription()
        );
    }
}
