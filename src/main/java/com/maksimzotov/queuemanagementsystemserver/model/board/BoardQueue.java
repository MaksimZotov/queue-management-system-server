package com.maksimzotov.queuemanagementsystemserver.model.board;

import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import lombok.Value;

import java.util.Comparator;
import java.util.List;

@Value
public class BoardQueue {
    String title;
    List<BoardPosition> positions;

    public static BoardQueue toModel(ClientInQueueRepo clientInQueueRepo, QueueEntity queueEntity) {
        return new BoardQueue(
                queueEntity.getName(),
                clientInQueueRepo.findAllByQueueId(queueEntity.getId()).get().stream()
                        .map((BoardPosition::toModel))
                        .sorted(Comparator.comparingInt(BoardPosition::getNumber))
                        .toList()
        );
    }
}
