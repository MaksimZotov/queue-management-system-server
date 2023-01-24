package com.maksimzotov.queuemanagementsystemserver.model.board;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.LocationEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import lombok.Value;

import java.util.List;

@Value
public class BoardModel {

    List<BoardQueue> queues;

    @JsonProperty("max_columns")
    Integer maxColumns;


    public static BoardModel toModel(
            ClientInQueueRepo clientInQueueRepo,
            List<QueueEntity> queues,
            LocationEntity locationEntity
    ) {
        return new BoardModel(
                queues.stream()
                        .map((queue) -> BoardQueue.toModel(clientInQueueRepo, queue))
                        .toList(),
                locationEntity.getMaxColumns()
        );
    }
}
