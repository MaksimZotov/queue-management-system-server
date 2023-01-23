package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.board.BoardModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface BoardService {
    BoardModel getLocationBoard(Localizer localizer, Long locationId) throws DescriptionException;
    void updateLocationBoard(Long locationId);
}
