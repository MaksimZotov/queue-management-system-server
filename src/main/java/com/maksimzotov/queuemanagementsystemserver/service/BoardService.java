package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;

public interface BoardService {
    BoardModel updateLocation(Long locationId) throws DescriptionException;
}
