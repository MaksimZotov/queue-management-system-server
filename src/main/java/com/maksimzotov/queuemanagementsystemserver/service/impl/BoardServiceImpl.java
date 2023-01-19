package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.config.WebSocketConfig;
import com.maksimzotov.queuemanagementsystemserver.entity.QueueEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;
import com.maksimzotov.queuemanagementsystemserver.repository.ClientInQueueRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.QueueRepo;
import com.maksimzotov.queuemanagementsystemserver.service.BoardService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final SimpMessagingTemplate messagingTemplate;
    private final QueueRepo queueRepo;
    private final ClientInQueueRepo clientInQueueRepo;

    @Override
    public BoardModel getLocationBoard(Localizer localizer, Long locationId) throws DescriptionException {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return BoardModel.toModel(
                clientInQueueRepo,
                queues.get()
        );
    }

    @Override
    public void updateLocationBoard(Long locationId)  {
        Optional<List<QueueEntity>> queues = queueRepo.findAllByLocationId(locationId);
        if (queues.isEmpty()) {
            return;
        }
        BoardModel boardModel = BoardModel.toModel(
                clientInQueueRepo,
                queues.get()
        );
        messagingTemplate.convertAndSend(
                WebSocketConfig.BOARD_URL + locationId,
                boardModel
        );
    }
}
