package com.maksimzotov.queuemanagementsystemserver.model.board;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.maksimzotov.queuemanagementsystemserver.entity.ClientInQueueEntity;
import lombok.Value;

@Value
public class BoardPosition {
    Integer number;
    @JsonProperty("public_code")
    Integer publicCode;

    public static BoardPosition toModel(ClientInQueueEntity clientInQueueEntity) {
        return new BoardPosition(
                clientInQueueEntity.getOrderNumber(),
                clientInQueueEntity.getCode()
        );
    }
}
